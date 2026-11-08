# COORDONATES BUKKIT PLUGIN

## What is it ?

This is a minecraft bukkit plugin providing new commands to help easily remember special or important places.

## How to use it ?

Simply add the jar file into the `plugins/` folder. Works on Spigot/Paper servers from **1.8** up to **26.x**.

There is 3 new commands available:

	/coord
Broadcasts the players position to everyone in the server

	/save-coord <label...>
Saves current coordonates with specified label. Label can have multiple words

	/show-coord
Shows the player's saved coordonates only to the player using the command

## How to build from source ?

There is no direct dependencies apart from bukkit itself, so the build steps are pretty easy. **You only need a JDK (17 or newer) installed**, the Gradle wrapper downloads Gradle for you.

After that, only one command is necessery

	./gradlew build

(on Windows: `gradlew.bat build`)

A jar file will be built under `build/libs/coord-plugin-2.0.jar`

`build` also runs `compatCheck`, which compiles the plugin against every supported Spigot API version (see `compatMinecraftVersions` in `build.gradle.kts`). To support a new Minecraft version, add it to that list.