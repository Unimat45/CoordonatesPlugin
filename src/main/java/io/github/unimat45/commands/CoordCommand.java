package io.github.unimat45.commands;

import org.bukkit.entity.Player;

import io.github.unimat45.storage.SavedLocation;

/**
 * Implementation of the command /coord: broadcasts the player's position to everyone
 */
public class CoordCommand extends PlayerCommand {
	@Override
	protected boolean execute(Player player, String[] args) {
		String position = SavedLocation.describePosition(player.getLocation());
		player.getServer().broadcastMessage(player.getName() + " is at " + position);
		return true;
	}
}
