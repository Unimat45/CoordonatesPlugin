package io.github.unimat45.Commands;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import io.github.unimat45.PlayerLocation;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;

/**
 * Implementation of the command /coord
 */
public class Coord implements CommandExecutor {
	private JavaPlugin plugin;

	/**
	 * Creates an instance of the command
	 * @param plugin Instance of the Main Plugin
	 */
	public Coord(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		// If the sender is not a player, returns incompletion
		if (!(sender instanceof Player)) {
			return false;
		}

		Player p = (Player) sender;

		// Location of the player
		PlayerLocation l = new PlayerLocation(p.getLocation());

		// Broadcast location to everyone in the server, including the player's username
		plugin.getServer().broadcastMessage(String.format("%s is at %s", p.getName(), l.toString()));

		return true;
	}
}