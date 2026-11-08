package io.github.unimat45;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.unimat45.commands.CoordCommand;
import io.github.unimat45.commands.DeleteCoordCommand;
import io.github.unimat45.commands.PendingOverwrites;
import io.github.unimat45.commands.SaveCoordCommand;
import io.github.unimat45.commands.ShowCoordCommand;
import io.github.unimat45.storage.LocationStore;
import io.github.unimat45.storage.YamlLocationFile;

/**
 * Main entry point of the plugin
 */
public class CoordPlugin extends JavaPlugin {
	private static final String FILE_NAME = "locations.yml";

	/**
	 * How long players have to repeat /save-coord to confirm replacing a label
	 */
	private static final long OVERWRITE_CONFIRM_MILLIS = 30_000;

	/**
	 * How long onDisable waits for queued saves before writing directly
	 */
	private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

	private LocationStore store;
	private YamlLocationFile locationFile;

	/**
	 * Writes the file off the main thread, one save at a time and in order.
	 * Null until the locations were loaded: nothing is ever written before that.
	 */
	private ExecutorService saveExecutor;

	@Override
	public void onEnable() {
		store = new LocationStore();
		locationFile = new YamlLocationFile(new File(getDataFolder(), FILE_NAME), getLogger());

		if (!loadLocations()) {
			// Disabling before saveExecutor exists, so onDisable won't overwrite the file
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		saveExecutor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, getName() + " save thread");
			thread.setDaemon(true);
			return thread;
		});

		Runnable onChange = this::saveAsync;
		getCommand("coord").setExecutor(new CoordCommand());
		getCommand("save-coord").setExecutor(
				new SaveCoordCommand(store, onChange, new PendingOverwrites(OVERWRITE_CONFIRM_MILLIS)));
		getCommand("show-coord").setExecutor(new ShowCoordCommand(store));
		getCommand("delete-coord").setExecutor(new DeleteCoordCommand(store, onChange));
	}

	@Override
	public void onDisable() {
		if (saveExecutor == null) {
			return;
		}

		// Let queued saves finish: the last one holds the latest state
		saveExecutor.shutdown();
		try {
			if (saveExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				saveExecutor = null;
				return;
			}
			getLogger().warning("Saving took more than " + SHUTDOWN_TIMEOUT_SECONDS + " seconds, saving directly");
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Drop stale queued saves and write the current state ourselves
		saveExecutor.shutdownNow();
		saveExecutor = null;
		write(locationFile.serialize(store));
	}

	/**
	 * Loads the saved locations into the store
	 * @return false if the plugin must not run, to avoid overwriting a file it couldn't read
	 */
	private boolean loadLocations() {
		try {
			int skipped = locationFile.load(store);
			if (skipped > 0) {
				Path backup = locationFile.backup("skipped", false);
				getLogger().warning("Skipped " + skipped + " invalid entries in " + locationFile.getPath()
						+ ", they will be removed on the next save. Original copied to " + backup);
			}

			int count = 0;
			for (UUID player : store.getPlayers()) {
				count += store.list(player).size();
			}
			getLogger().info("Loaded " + count + " saved coordinates of " + store.getPlayers().size() + " players");
			return true;
		}
		catch (InvalidConfigurationException e) {
			// Unreadable YAML: keep it aside for manual recovery and start fresh
			store.clear();
			try {
				Path backup = locationFile.backup("broken", true);
				getLogger().log(Level.SEVERE, locationFile.getPath() + " is not valid YAML. Moved it to " + backup
						+ " and starting with no saved coordinates", e);
				return true;
			}
			catch (IOException backupError) {
				getLogger().log(Level.SEVERE, locationFile.getPath() + " is not valid YAML and could not be moved"
						+ " aside, disabling the plugin. Fix or remove the file", backupError);
				return false;
			}
		}
		catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not load " + locationFile.getPath()
					+ ", disabling the plugin to avoid overwriting it", e);
			return false;
		}
	}

	/**
	 * Serializes the store now (on the main thread), then writes it in the background
	 */
	private void saveAsync() {
		String data = locationFile.serialize(store);
		saveExecutor.execute(() -> write(data));
	}

	private void write(String data) {
		try {
			locationFile.write(data);
		}
		catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not save " + locationFile.getPath(), e);
		}
	}
}
