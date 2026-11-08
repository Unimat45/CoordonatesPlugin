package io.github.unimat45.storage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Reads and writes saved locations as a YAML file.
 *
 * Format:
 *
 * version: 1
 * players:
 *   player-uuid:
 *   - label: Home
 *     world: world
 *     x: 10
 *     y: 64
 *     z: -3
 *
 * Locations are stored as a list rather than keyed by label, because Bukkit treats
 * dots in keys as path separators and labels are free text.
 */
public class YamlLocationFile {
	/**
	 * Current version of the file format
	 */
	public static final int FORMAT_VERSION = 1;

	private final Path file;
	private final Logger logger;

	public YamlLocationFile(File file, Logger logger) {
		this.file = file.toPath();
		this.logger = logger;
	}

	public Path getPath() {
		return file;
	}

	/**
	 * Replaces the store's content with the file's. A missing or empty file loads as empty.
	 * Malformed entries are skipped with a warning.
	 * @return The number of skipped entries
	 * @throws InvalidConfigurationException The file isn't valid YAML
	 * @throws IOException The file can't be read, or was written by a newer version of the plugin
	 */
	public int load(LocationStore store) throws IOException, InvalidConfigurationException {
		store.clear();
		if (!Files.exists(file)) {
			return 0;
		}

		// Read as UTF-8 ourselves: older Bukkit versions use the platform charset in load(File)
		String data = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
		if (data.trim().isEmpty()) {
			return 0;
		}

		YamlConfiguration yaml = new YamlConfiguration();
		yaml.loadFromString(data);

		int version = yaml.getInt("version", FORMAT_VERSION);
		if (version > FORMAT_VERSION) {
			throw new IOException(file + " uses format version " + version
					+ ", but this plugin only supports up to " + FORMAT_VERSION + ". Update the plugin.");
		}

		ConfigurationSection players = yaml.getConfigurationSection("players");
		if (players == null) {
			return 0;
		}

		int skipped = 0;
		for (String uuidString : players.getKeys(false)) {
			UUID player;
			try {
				player = UUID.fromString(uuidString);
			}
			catch (IllegalArgumentException e) {
				logger.warning("Skipping locations of invalid player UUID '" + uuidString + "' in " + file);
				skipped += players.getMapList(uuidString).size();
				continue;
			}

			for (Map<?, ?> entry : players.getMapList(uuidString)) {
				SavedLocation location = parseLocation(entry);
				if (location == null) {
					logger.warning("Skipping invalid location " + entry + " of player " + uuidString + " in " + file);
					skipped++;
				}
				else if (store.put(player, location) != null) {
					logger.warning("Duplicate label '" + location.getLabel() + "' for player " + uuidString
							+ " in " + file + ", keeping the last one");
				}
			}
		}
		return skipped;
	}

	/**
	 * @return The location described by a YAML entry, or null if the entry is malformed
	 */
	private static SavedLocation parseLocation(Map<?, ?> entry) {
		Object label = entry.get("label");
		Object world = entry.get("world");
		Object x = entry.get("x");
		Object y = entry.get("y");
		Object z = entry.get("z");

		if (label == null || world == null
				|| !(x instanceof Number) || !(y instanceof Number) || !(z instanceof Number)) {
			return null;
		}

		// String.valueOf: a label like "123" or "yes" may have been hand-edited without quotes
		String labelString = String.valueOf(label).trim();
		String worldString = String.valueOf(world);
		if (labelString.isEmpty() || worldString.isEmpty()) {
			return null;
		}

		return new SavedLocation(labelString, worldString,
				((Number) x).intValue(), ((Number) y).intValue(), ((Number) z).intValue());
	}

	/**
	 * Serializes the store. Call it on the main thread, then write() the result from any thread.
	 */
	public String serialize(LocationStore store) {
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.set("version", FORMAT_VERSION);

		// Sorted for stable, diff-friendly output
		List<UUID> players = new ArrayList<>(store.getPlayers());
		players.sort(null);

		for (UUID player : players) {
			List<Map<String, Object>> entries = new ArrayList<>();
			for (SavedLocation location : store.list(player)) {
				Map<String, Object> entry = new LinkedHashMap<>();
				entry.put("label", location.getLabel());
				entry.put("world", location.getWorld());
				entry.put("x", location.getX());
				entry.put("y", location.getY());
				entry.put("z", location.getZ());
				entries.add(entry);
			}
			yaml.set("players." + player, entries);
		}

		// Keep the key even when empty, so the file always shows its structure
		if (players.isEmpty()) {
			yaml.createSection("players");
		}

		return yaml.saveToString();
	}

	/**
	 * Atomically replaces the file with the given content: writes a temporary file next to it,
	 * then moves it over the real one, so a crash never leaves a half-written file.
	 * Synchronized so concurrent writes can't interleave.
	 */
	public synchronized void write(String data) throws IOException {
		Path dir = file.toAbsolutePath().getParent();
		Files.createDirectories(dir);

		Path tmp = dir.resolve(file.getFileName() + ".tmp");
		Files.write(tmp, data.getBytes(StandardCharsets.UTF_8));

		try {
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		}
		catch (AtomicMoveNotSupportedException e) {
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Keeps a copy of the current file, e.g. before it gets overwritten after a failed load
	 * @param suffix Describes why, e.g. "broken"
	 * @param move Moves the file instead of copying it
	 * @return The backup path, or null if there was no file to back up
	 */
	public synchronized Path backup(String suffix, boolean move) throws IOException {
		if (!Files.exists(file)) {
			return null;
		}

		String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
		Path target = file.resolveSibling(file.getFileName() + "." + suffix + "-" + timestamp);

		if (move) {
			Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
		}
		else {
			Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
		}
		return target;
	}
}
