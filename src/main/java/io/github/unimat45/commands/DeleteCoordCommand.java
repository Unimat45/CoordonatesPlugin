package io.github.unimat45.commands;

import org.bukkit.entity.Player;

import io.github.unimat45.storage.LocationStore;
import io.github.unimat45.storage.SavedLocation;

/**
 * Implementation of the command /delete-coord: deletes one of the player's saved locations
 */
public class DeleteCoordCommand extends PlayerCommand {
	private final LocationStore store;
	private final Runnable onChange;

	/**
	 * @param onChange Called after every change to the store, e.g. to save it
	 */
	public DeleteCoordCommand(LocationStore store, Runnable onChange) {
		this.store = store;
		this.onChange = onChange;
	}

	@Override
	protected boolean execute(Player player, String[] args) {
		String label = joinLabel(args);
		if (label.isEmpty()) {
			return false;
		}

		SavedLocation removed = store.remove(player.getUniqueId(), label);
		if (removed == null) {
			player.sendMessage("You have no coordinates saved as '" + label + "'.");
			return true;
		}

		onChange.run();
		player.sendMessage("Deleted '" + removed.getLabel() + "' (" + removed.describePosition() + ").");
		return true;
	}
}
