package io.github.unimat45.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.unimat45.LocationMap;
import io.github.unimat45.PlayerLocation;

/**
 * Implementation of the command /save-coord
 */
public class SaveCoord implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		// If the sender is not a player, returns incompletion
		if (!(sender instanceof Player)) {
			return false;
		}

		Player p = (Player) sender;

		// Joins all the arguments into one string with spaces
		String label = String.join(" ", args);
		PlayerLocation loc = new PlayerLocation(p.getLocation());

		// Adds location to HashMap, using the players UUID, label and location
		LocationMap.addLocation(p.getUniqueId().toString(), label, loc);

		// Sends message to sender only, confirming the saved location
		p.sendMessage("Saved place: " + label + " at " + loc.toString());

		return true;
	}
}
