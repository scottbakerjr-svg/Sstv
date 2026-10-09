# Baker's Dozen

Android TV media center with a Kodi-inspired interface. This project is not affiliated with Kodi, SuperBox, The Crew, or POV.

## Kodi and requested add-ons

Requested video add-ons: **The Crew** and **POV**.

**Important:** This APK does not currently bundle Kodi, The Crew, or POV, and cannot execute Kodi Python plug-ins. The included KodiZipStager and KodiAddonInstaller only validate/stage ZIP packages inside this application's private storage. They do not install add-ons into Kodi or provide a Kodi Python runtime.

For functional Kodi add-ons, install the official Kodi Android application separately and install authorized add-ons from their respective trusted sources inside Kodi. A future version of Baker's Dozen can detect and launch Kodi, but a standalone Android app cannot silently preinstall add-ons into another app's private data. Third-party add-ons and their dependencies must be reviewed for licensing and authorized media sources before bundling.

## Build

GitHub Actions builds a debug APK from the Android project. Go to Actions > Build APK, choose a successful run, and download the build artifact. The presence of an artifact does not establish Kodi plug-in compatibility.

Android project package: `com.example.s8styletv` (unchanged to preserve upgrade compatibility).
