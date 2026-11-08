package io.github.unimat45.commands;

import org.bukkit.entity.Player;

import io.github.unimat45.storage.LocationStore;
import io.github.unimat45.storage.SavedLocation;

/**
 * Implementation of the command /save-coord: saves the player's position under a label.
 * Replacing an existing label (ignoring case) must be confirmed by running the command again.
 */
public class SaveCoordCommand extends PlayerCommand {
	private final LocationStore store;
	private final Runnable onChange;
	private final PendingOverwrites pendingOverwrites;

	/**
	 * @param onChange Called after every change to the store, e.g. to save it
	 */
	public SaveCoordCommand(LocationStore store, Runnable onChange, PendingOverwrites pendingOverwrites) {
		this.store = store;
		this.onChange = onChange;
		this.pendingOverwrites = pendingOverwrites;
	}

	@Override
	protected boolean execute(Player player, String[] args) {
		String label = joinLabel(args);
		if (label.isEmpty()) {
			return false;
		}

		SavedLocation location = SavedLocation.of(label, player.getLocation());
		SavedLocation existing = store.get(player.getUniqueId(), label);

		if (existing == null) {
			pendingOverwrites.clear(player.getUniqueId());
		}
		else if (!pendingOverwrites.confirmOrRequest(player.getUniqueId(), label)) {
			long seconds = pendingOverwrites.getTimeoutMillis() / 1000;
			player.sendMessage("You already saved '" + existing.getLabel() + "' at " + existing.describePosition() + ".");
			player.sendMessage("Run /save-coord " + label + " again within " + seconds
					+ " seconds to replace it with your current position.");
			return true;
		}

		store.put(player.getUniqueId(), location);
		onChange.run();

		player.sendMessage((existing == null ? "Saved '" : "Replaced '") + label + "' at " + location.describePosition());
		return true;
	}
}
