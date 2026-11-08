# COORDONATES BUKKIT PLUGIN

## What is it ?

This is a minecraft bukkit plugin providing new commands to help easily remember special or important places.

## How to use it ?

Simply add the jar file into the `plugins/` folder. Works on Spigot/Paper servers from **1.8** up to **26.x**.

There is 4 new commands available:

	/coord
Broadcasts the players position to everyone in the server

	/save-coord <label...>
Saves current coordonates and world with specified label. Label can have multiple words and is case-insensitive. Saving over an existing label asks for confirmation: run the same command again within 30 seconds to replace it

	/show-coord
Shows the player's saved coordonates only to the player using the command

	/delete-coord <label...>
Deletes one of the player's saved coordonates

Every command has a permission (`coord.use`, `coord.save`, `coord.show`, `coord.delete`, or `coord.*` for all), given to everyone by default.

Saved coordonates are stored in `plugins/coordonates-finder/locations.yml`, written after every change. If the file can't be read as YAML, it is moved aside to `locations.yml.broken-<date>` and the plugin starts with no saved coordonates.

## How to build from source ?

There is no direct dependencies apart from bukkit itself, so the build steps are pretty easy. **You only need a JDK (17 or newer) installed**, the Gradle wrapper downloads Gradle for you.

After that, only one command is necessery

	./gradlew build

(on Windows: `gradlew.bat build`)

A jar file will be built under `build/libs/coord-plugin-3.0.jar`

`build` also runs `compatCheck`, which compiles the plugin against every supported Spigot API version (see `compatMinecraftVersions` in `build.gradle.kts`). To support a new Minecraft version, add it to that list.

The tests run twice: against the oldest supported API (`test`) and the newest one (`testAgainstLatestApi`), since servers bundle different versions of the YAML library.