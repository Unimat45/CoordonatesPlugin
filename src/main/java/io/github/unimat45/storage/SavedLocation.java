package io.github.unimat45.storage;

import java.util.Locale;

import org.bukkit.Location;

/**
 * A labelled block position saved by a player. Immutable.
 */
public final class SavedLocation {
	private final String label;
	private final String world;
	private final int x;
	private final int y;
	private final int z;

	public SavedLocation(String label, String world, int x, int y, int z) {
		if (label == null || label.isEmpty()) {
			throw new IllegalArgumentException("label must not be empty");
		}
		if (world == null || world.isEmpty()) {
			throw new IllegalArgumentException("world must not be empty");
		}
		this.label = label;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Captures the block position of a Bukkit location
	 */
	public static SavedLocation of(String label, Location location) {
		return new SavedLocation(label, location.getWorld().getName(),
				location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * Normalizes a label for case-insensitive lookups
	 */
	public static String key(String label) {
		return label.toLowerCase(Locale.ROOT);
	}

	public String getLabel() {
		return label;
	}

	public String getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	/**
	 * Formats the position, e.g. "x: 10, y: 64, z: -3 in world"
	 */
	public String describePosition() {
		return describePosition(world, x, y, z);
	}

	/**
	 * Formats the block position of a Bukkit location, e.g. "x: 10, y: 64, z: -3 in world"
	 */
	public static String describePosition(Location location) {
		return describePosition(location.getWorld().getName(),
				location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	private static String describePosition(String world, int x, int y, int z) {
		return String.format("x: %d, y: %d, z: %d in %s", x, y, z, world);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SavedLocation)) {
			return false;
		}
		SavedLocation other = (SavedLocation) o;
		return x == other.x && y == other.y && z == other.z
				&& label.equals(other.label) && world.equals(other.world);
	}

	@Override
	public int hashCode() {
		int h = label.hashCode();
		h = 31 * h + world.hashCode();
		h = 31 * h + x;
		h = 31 * h + y;
		h = 31 * h + z;
		return h;
	}

	@Override
	public String toString() {
		return label + " (" + describePosition() + ")";
	}
}
