package io.github.unimat45.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Base class for commands that only players can use
 */
public abstract class PlayerCommand implements CommandExecutor {
	@Override
	public final boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can use this command.");
			return true;
		}
		return execute((Player) sender, args);
	}

	/**
	 * Runs the command for a player
	 * @return false to show the command's usage
	 */
	protected abstract boolean execute(Player player, String[] args);

	/**
	 * Joins all arguments into a single label, e.g. "/save-coord my base" -> "my base"
	 * @return The label, empty if there were no arguments
	 */
	protected static String joinLabel(String[] args) {
		return String.join(" ", args).trim();
	}
}
