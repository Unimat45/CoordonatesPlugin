package io.github.unimat45.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * In-memory saved locations, per player. Labels are case-insensitive.
 *
 * Not thread-safe: only use it from the server's main thread.
 */
public class LocationStore {
	/**
	 * Player UUID -> lowercased label -> location
	 */
	private final Map<UUID, Map<String, SavedLocation>> players = new HashMap<>();

	/**
	 * Finds a player's location by label, ignoring case
	 * @return The location, or null if there is none with that label
	 */
	public SavedLocation get(UUID player, String label) {
		Map<String, SavedLocation> locations = players.get(player);
		return locations == null ? null : locations.get(SavedLocation.key(label));
	}

	/**
	 * Saves a location, replacing any existing one with the same label (ignoring case)
	 * @return The replaced location, or null if there was none
	 */
	public SavedLocation put(UUID player, SavedLocation location) {
		Map<String, SavedLocation> locations = players.get(player);
		if (locations == null) {
			locations = new HashMap<>();
			players.put(player, locations);
		}
		return locations.put(SavedLocation.key(location.getLabel()), location);
	}

	/**
	 * Removes a location by label, ignoring case
	 * @return The removed location, or null if there was none
	 */
	public SavedLocation remove(UUID player, String label) {
		Map<String, SavedLocation> locations = players.get(player);
		if (locations == null) {
			return null;
		}
		SavedLocation removed = locations.remove(SavedLocation.key(label));
		if (locations.isEmpty()) {
			players.remove(player);
		}
		return removed;
	}

	/**
	 * Lists a player's locations, sorted by label (ignoring case)
	 * @return A read-only list, empty if the player has none
	 */
	public List<SavedLocation> list(UUID player) {
		Map<String, SavedLocation> locations = players.get(player);
		if (locations == null) {
			return Collections.emptyList();
		}
		List<SavedLocation> sorted = new ArrayList<>(locations.values());
		Collections.sort(sorted, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getLabel(), b.getLabel()));
		return Collections.unmodifiableList(sorted);
	}

	/**
	 * @return A read-only view of every player that has at least one location
	 */
	public Set<UUID> getPlayers() {
		return Collections.unmodifiableSet(players.keySet());
	}

	public void clear() {
		players.clear();
	}
}
