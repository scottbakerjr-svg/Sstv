package com.example.s8styletv

import android.content.Context
import org.w3c.dom.Element
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/** Install-time dependency metadata and atomic promotion of validated ZIPs. */
internal object KodiAddonInstaller {
 data class Dependency(val id:String,val version:String,val optional:Boolean)
 data class Installed(val manifest:KodiZipStager.Manifest,val dependencies:List<Dependency>,val directory:File)

 fun install(context:Context,archive:InputStream):Installed {
  val root=File(context.filesDir,"kodi_addons").apply{mkdirs()}
  val temp=File(context.cacheDir,"kodi_stage_"+System.nanoTime())
  val manifest=KodiZipStager.stage(archive,temp)
  val dependencies=readDependencies(temp)
  val unresolved=dependencies.filter { !it.optional && it.id!="xbmc.python" && !File(root,it.id).isDirectory }
  require(unresolved.isEmpty()){"Missing dependencies: "+unresolved.joinToString { it.id }}
  val destination=File(root,manifest.id)
  require(!destination.exists()){"Add-on already installed; remove it before updating"}
  require(temp.renameTo(destination)){"Could not finalize installation"}
  return Installed(manifest,dependencies,destination)
 }

 private fun readDependencies(stage:File):List<Dependency> {
  val file=stage.walkTopDown().firstOrNull{it.isFile && it.name=="addon.xml"}?:return emptyList()
  val factory=DocumentBuilderFactory.newInstance()
  factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true)
  factory.setFeature("http://xml.org/sax/features/external-general-entities",false)
  factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false)
  factory.isXIncludeAware=false
  factory.isExpandEntityReferences=false
  val nodes=factory.newDocumentBuilder().parse(file).getElementsByTagName("import")
  return (0 until nodes.length).mapNotNull { i ->
   val e=nodes.item(i) as? Element ?: return@mapNotNull null
   e.getAttribute("addon").takeIf{it.isNotBlank()}?.let{Dependency(it,e.getAttribute("version"),e.getAttribute("optional")=="true")}
  }
 }
}
