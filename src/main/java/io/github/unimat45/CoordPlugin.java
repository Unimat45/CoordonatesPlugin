package io.github.unimat45;

import java.io.IOException;

import org.bukkit.plugin.java.JavaPlugin;
import io.github.unimat45.Commands.Coord;
import io.github.unimat45.Commands.SaveCoord;
import io.github.unimat45.Commands.ShowCoord;

/**
 * Main entry point of the plugin
 */
public class CoordPlugin extends JavaPlugin {
    @Override
    public void onDisable() {
        // Writes to JSON file
        try {
            LocationMap.DumpToFile();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        // Loads from JSON file
        try {
            LocationMap.LoadFromFile();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Enables All Commands
        this.getCommand("coord").setExecutor(new Coord(this));
        this.getCommand("save-coord").setExecutor(new SaveCoord());
        this.getCommand("show-coord").setExecutor(new ShowCoord());
    }
}
