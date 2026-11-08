package io.github.unimat45.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class YamlLocationFileTest {
	private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");

	@TempDir
	Path dir;

	private Path path;
	private YamlLocationFile file;

	@BeforeEach
	void setUp() {
		path = dir.resolve("data").resolve("locations.yml");
		file = new YamlLocationFile(path.toFile(), Logger.getAnonymousLogger());
	}

	private void writeRaw(String content) throws IOException {
		Files.createDirectories(path.getParent());
		Files.write(path, content.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void roundTripsEveryField() throws Exception {
		LocationStore store = new LocationStore();
		store.put(ALICE, new SavedLocation("Home", "world", 10, 64, -3));
		store.put(ALICE, new SavedLocation("Nether hub", "world_nether", -100, 32, 7));
		store.put(BOB, new SavedLocation("base.with.dots", "world", 0, 0, 0));

		file.write(file.serialize(store));

		LocationStore loaded = new LocationStore();
		assertEquals(0, file.load(loaded));
		assertEquals(store.list(ALICE), loaded.list(ALICE));
		assertEquals(store.list(BOB), loaded.list(BOB));
	}

	@Test
	void keepsNonAsciiAndYamlLookingLabels() throws Exception {
		LocationStore store = new LocationStore();
		for (String label : Arrays.asList("Château d'été", "村", "123", "yes", "null", "a: b", "- dash", "#hash")) {
			store.put(ALICE, new SavedLocation(label, "world", 1, 2, 3));
		}

		file.write(file.serialize(store));

		LocationStore loaded = new LocationStore();
		assertEquals(0, file.load(loaded));
		assertEquals(store.list(ALICE), loaded.list(ALICE));
	}

	@Test
	void missingFileLoadsEmpty() throws Exception {
		LocationStore store = new LocationStore();
		store.put(ALICE, new SavedLocation("Home", "world", 1, 2, 3));

		assertEquals(0, file.load(store));
		assertTrue(store.getPlayers().isEmpty(), "load replaces the previous content");
	}

	@Test
	void emptyFileLoadsEmpty() throws Exception {
		for (String content : Arrays.asList("", " \n\n", "version: 1\n", "players:\n", "players: {}\n")) {
			writeRaw(content);
			LocationStore store = new LocationStore();
			assertEquals(0, file.load(store), content);
			assertTrue(store.getPlayers().isEmpty(), content);
		}
	}

	@Test
	void invalidYamlThrows() throws Exception {
		writeRaw("players: [unclosed\n  - {");
		assertThrows(InvalidConfigurationException.class, () -> file.load(new LocationStore()));
	}

	@Test
	void newerFormatVersionIsRefused() throws Exception {
		writeRaw("version: " + (YamlLocationFile.FORMAT_VERSION + 1) + "\nplayers: {}\n");
		assertThrows(IOException.class, () -> file.load(new LocationStore()));
	}

	@Test
	void skipsInvalidEntriesAndKeepsValidOnes() throws Exception {
		writeRaw("version: 1\n"
				+ "players:\n"
				+ "  not-a-uuid:\n"
				+ "  - {label: Lost, world: world, x: 1, y: 2, z: 3}\n"
				+ "  " + ALICE + ":\n"
				+ "  - {label: Good, world: world, x: 1, y: 2, z: 3}\n"
				+ "  - {label: NoCoords, world: world}\n"
				+ "  - {label: BadX, world: world, x: abc, y: 2, z: 3}\n"
				+ "  - {world: world, x: 1, y: 2, z: 3}\n");

		LocationStore store = new LocationStore();
		assertEquals(4, file.load(store));
		assertEquals(Arrays.asList(new SavedLocation("Good", "world", 1, 2, 3)), store.list(ALICE));
	}

	@Test
	void writeReplacesFileAndLeavesNoTemporaryFile() throws Exception {
		writeRaw("old content");

		LocationStore store = new LocationStore();
		store.put(ALICE, new SavedLocation("Home", "world", 1, 2, 3));
		file.write(file.serialize(store));

		LocationStore loaded = new LocationStore();
		file.load(loaded);
		assertEquals(store.list(ALICE), loaded.list(ALICE));
		assertFalse(Files.exists(path.resolveSibling("locations.yml.tmp")));
	}

	@Test
	void writeCreatesMissingDirectories() throws Exception {
		assertFalse(Files.exists(path.getParent()));
		file.write(file.serialize(new LocationStore()));
		assertTrue(Files.exists(path));
	}

	@Test
	void backupMovesOrCopiesTheFile() throws Exception {
		writeRaw("content");

		Path copy = file.backup("skipped", false);
		assertNotNull(copy);
		assertTrue(Files.exists(path), "copy keeps the original");
		assertEquals("content", new String(Files.readAllBytes(copy), StandardCharsets.UTF_8));
		assertTrue(copy.getFileName().toString().startsWith("locations.yml.skipped-"));

		Path moved = file.backup("broken", true);
		assertNotNull(moved);
		assertFalse(Files.exists(path), "move removes the original");
		assertTrue(Files.exists(moved));
	}

	@Test
	void backupOfMissingFileReturnsNull() throws Exception {
		assertEquals(null, new YamlLocationFile(new File(dir.toFile(), "none.yml"), Logger.getAnonymousLogger())
				.backup("broken", true));
	}
}
