package io.github.unimat45.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PendingOverwritesTest {
	private static final UUID ALICE = UUID.randomUUID();
	private static final UUID BOB = UUID.randomUUID();

	private long now = 1_000;
	private final PendingOverwrites pending = new PendingOverwrites(30_000, () -> now);

	@Test
	void secondRequestWithinTimeoutConfirms() {
		assertFalse(pending.confirmOrRequest(ALICE, "Home"));
		now += 30_000;
		assertTrue(pending.confirmOrRequest(ALICE, "HOME"), "same label, ignoring case");
	}

	@Test
	void confirmationIsUsedOnlyOnce() {
		assertFalse(pending.confirmOrRequest(ALICE, "Home"));
		assertTrue(pending.confirmOrRequest(ALICE, "Home"));
		assertFalse(pending.confirmOrRequest(ALICE, "Home"));
	}

	@Test
	void expiredRequestStartsOver() {
		assertFalse(pending.confirmOrRequest(ALICE, "Home"));
		now += 30_001;
		assertFalse(pending.confirmOrRequest(ALICE, "Home"));
		assertTrue(pending.confirmOrRequest(ALICE, "Home"));
	}

	@Test
	void otherLabelReplacesTheRequest() {
		assertFalse(pending.confirmOrRequest(ALICE, "Home"));
		assertFalse(pending.confirmOrRequest(ALICE, "Mine"));
		assertFalse(pending.confirmOrRequest(ALICE, "Home"));
	}

	@Test
	void playersAreIndependentAndClearForgets() {
		assertFalse(pending.confirmOrRequest(ALICE, "Home"));
		assertFalse(pending.confirmOrRequest(BOB, "Home"));
		pending.clear(BOB);
		assertTrue(pending.confirmOrRequest(ALICE, "Home"));
		assertFalse(pending.confirmOrRequest(BOB, "Home"));
	}
}
