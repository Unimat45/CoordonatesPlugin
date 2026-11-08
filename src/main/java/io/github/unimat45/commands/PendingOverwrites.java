package io.github.unimat45.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

import io.github.unimat45.storage.SavedLocation;

/**
 * Tracks overwrites waiting for the player to confirm them by repeating the command.
 * One pending overwrite per player; a new request replaces the previous one.
 */
public class PendingOverwrites {
	private final long timeoutMillis;
	private final LongSupplier clock;

	/**
	 * Player UUID -> pending overwrite
	 */
	private final Map<UUID, Pending> pending = new HashMap<>();

	public PendingOverwrites(long timeoutMillis) {
		this(timeoutMillis, System::currentTimeMillis);
	}

	PendingOverwrites(long timeoutMillis, LongSupplier clock) {
		this.timeoutMillis = timeoutMillis;
		this.clock = clock;
	}

	public long getTimeoutMillis() {
		return timeoutMillis;
	}

	/**
	 * Confirms the overwrite of a label if the player asked for it recently, or starts a new
	 * confirmation otherwise.
	 * @return true if this call confirms an earlier request, false if a new one was started
	 */
	public boolean confirmOrRequest(UUID player, String label) {
		String key = SavedLocation.key(label);
		long now = clock.getAsLong();

		Pending previous = pending.remove(player);
		if (previous != null && previous.key.equals(key) && now <= previous.expiresAt) {
			return true;
		}

		pending.put(player, new Pending(key, now + timeoutMillis));
		return false;
	}

	/**
	 * Forgets any pending overwrite of a player
	 */
	public void clear(UUID player) {
		pending.remove(player);
	}

	private static final class Pending {
		final String key;
		final long expiresAt;

		Pending(String key, long expiresAt) {
			this.key = key;
			this.expiresAt = expiresAt;
		}
	}
}
