package io.github.unimat45.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class LocationStoreTest {
	private static final UUID ALICE = UUID.randomUUID();
	private static final UUID BOB = UUID.randomUUID();

	private final LocationStore store = new LocationStore();

	@Test
	void labelsIgnoreCase() {
		SavedLocation home = new SavedLocation("Home", "world", 1, 2, 3);
		store.put(ALICE, home);

		assertEquals(home, store.get(ALICE, "HOME"));
		assertEquals(home, store.get(ALICE, "home"));
	}

	@Test
	void putReplacesSameLabelIgnoringCaseAndKeepsNewSpelling() {
		SavedLocation first = new SavedLocation("Home", "world", 1, 2, 3);
		SavedLocation second = new SavedLocation("HOME", "world", 4, 5, 6);

		assertNull(store.put(ALICE, first));
		assertEquals(first, store.put(ALICE, second));
		assertEquals(Arrays.asList(second), store.list(ALICE));
	}

	@Test
	void playersAreIndependent() {
		store.put(ALICE, new SavedLocation("Home", "world", 1, 2, 3));
		assertNull(store.get(BOB, "Home"));
		assertTrue(store.list(BOB).isEmpty());
	}

	@Test
	void removeIgnoresCaseAndForgetsEmptyPlayers() {
		SavedLocation home = new SavedLocation("Home", "world", 1, 2, 3);
		store.put(ALICE, home);

		assertNull(store.remove(ALICE, "other"));
		assertNull(store.remove(BOB, "home"));
		assertEquals(home, store.remove(ALICE, "hOmE"));
		assertTrue(store.getPlayers().isEmpty());
	}

	@Test
	void listIsSortedIgnoringCaseAndReadOnly() {
		store.put(ALICE, new SavedLocation("charlie", "world", 0, 0, 0));
		store.put(ALICE, new SavedLocation("Alpha", "world", 0, 0, 0));
		store.put(ALICE, new SavedLocation("bravo", "world", 0, 0, 0));

		List<SavedLocation> list = store.list(ALICE);
		assertEquals("Alpha", list.get(0).getLabel());
		assertEquals("bravo", list.get(1).getLabel());
		assertEquals("charlie", list.get(2).getLabel());
		assertThrows(UnsupportedOperationException.class, () -> list.clear());
	}
}
