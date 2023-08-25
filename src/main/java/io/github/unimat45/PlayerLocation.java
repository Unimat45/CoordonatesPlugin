package io.github.unimat45;

import org.bukkit.Location;

/**
 * Represents a player's location in the world
 */
public class PlayerLocation {
	private int x;	
	private int y;	
	private int z;
	
	public PlayerLocation(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public PlayerLocation(Location l) {
		this.x = l.getBlockX();
		this.y = l.getBlockY();
		this.z = l.getBlockZ();
	}

	@Override
	public String toString() {
		return String.format("x: %d, y: %d, z: %d", this.x, this.y, this.z);
	}
}
