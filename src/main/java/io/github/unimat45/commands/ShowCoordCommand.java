package io.github.unimat45.commands;

import java.util.List;

import org.bukkit.entity.Player;

import io.github.unimat45.storage.LocationStore;
import io.github.unimat45.storage.SavedLocation;

/**
 * Implementation of the command /show-coord: lists the player's saved locations, only to them
 */
public class ShowCoordCommand extends PlayerCommand {
	private final LocationStore store;

	public ShowCoordCommand(LocationStore store) {
		this.store = store;
	}

	@Override
	protected boolean execute(Player player, String[] args) {
		List<SavedLocation> locations = store.list(player.getUniqueId());

		if (locations.isEmpty()) {
			player.sendMessage("No coordinates saved. Use /save-coord <label> to save your position.");
			return true;
		}

		String[] lines = new String[locations.size() + 1];
		lines[0] = "Your saved coordinates (" + locations.size() + "):";
		for (int i = 0; i < locations.size(); i++) {
			SavedLocation location = locations.get(i);
			lines[i + 1] = "- " + location.getLabel() + ": " + location.describePosition();
		}
		player.sendMessage(lines);

		return true;
	}
}
