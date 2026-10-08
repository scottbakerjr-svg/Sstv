package com.example.s8styletv

import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Safe staging for Kodi add-on archives. This does not execute Python.
 * A compatible embedded Python runtime and Kodi API bridge are required for execution.
 */
internal object KodiZipStager {
    data class Manifest(val id: String, val name: String, val version: String, val entrypoint: String?)
    private const val MAX_ENTRIES = 2000
    private const val MAX_BYTES = 80L * 1024 * 1024

    fun stage(input: InputStream, target: File): Manifest {
        require(!target.exists()) { "Target already exists" }
        require(target.mkdirs()) { "Cannot create staging directory" }
        var count = 0
        var total = 0L
        try {
            ZipInputStream(input).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    count++
                    require(count <= MAX_ENTRIES) { "Archive contains too many files" }
                    val name = entry.name.replace('\\', '/')
                    require(!name.startsWith("/") && !name.contains("../") && !name.contains("\u0000")) { "Unsafe archive path" }
                    val dest = File(target, name)
                    require(dest.canonicalPath.startsWith(target.canonicalPath + File.separator)) { "Archive escapes staging directory" }
                    if (entry.isDirectory) {
                        require(dest.mkdirs() || dest.isDirectory)
                    } else {
                        require(dest.parentFile?.mkdirs() == true || dest.parentFile?.isDirectory == true)
                        dest.outputStream().use { out ->
                            val buffer = ByteArray(8192)
                            while (true) {
                                val n = zip.read(buffer)
                                if (n < 0) break
                                total += n
                                require(total <= MAX_BYTES) { "Archive exceeds size limit" }
                                out.write(buffer, 0, n)
                            }
                        }
                    }
                    zip.closeEntry()
                }
            }
            val manifests = target.walkTopDown().filter { it.isFile && it.name == "addon.xml" }.take(2).toList()
            require(manifests.size == 1) { "Expected exactly one Kodi addon.xml" }
            val factory = DocumentBuilderFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            factory.isXIncludeAware = false
            factory.isExpandEntityReferences = false
            val root = factory.newDocumentBuilder().parse(manifests[0]).documentElement
            require(root.tagName == "addon") { "Not a Kodi add-on manifest" }
            val id = root.getAttribute("id")
            require(id.matches(Regex("[A-Za-z0-9_.-]{1,160}"))) { "Invalid add-on ID" }
            val extensions = root.getElementsByTagName("extension")
            var library: String? = null
            for (i in 0 until extensions.length) {
                val element = extensions.item(i) as? org.w3c.dom.Element ?: continue
                if (element.getAttribute("point") == "xbmc.python.pluginsource") {
                    library = element.getAttribute("library").takeIf { it.isNotBlank() }
                }
            }
            return Manifest(id, root.getAttribute("name"), root.getAttribute("version"), library)
        } catch (e: Exception) {
            target.deleteRecursively()
            throw e
        }
    }
}
