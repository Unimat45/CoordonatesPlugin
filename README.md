# COORDONATES BUKKIT PLUGIN

## What is it ?

This is a minecraft bukkit plugin providing new commands to help easily remember special or important places.

## How to use it ?

Simply add the jar file into the `plugins/` folder.

There is 3 new commands available:

	/coord
Broadcasts the players position to everyone in the server

	/save-coord <label...>
Saves current coordonates with specified label. Label can have multiple words

	/show-coord
Shows the player's saved coordonates only to the player using the command

## How to build from source ?

There is no direct dependencies apart from bukkit itself, so the build steps are pretty easy. **You must have `Maven` installed**

After that, only one command is necessery

	mvn package

A jar file will be built under `target/coord-plugin-2.0.jar`