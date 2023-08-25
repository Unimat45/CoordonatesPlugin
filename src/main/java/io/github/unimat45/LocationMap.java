package io.github.unimat45;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/*
 * Structure for stored data:
 * 
 * {
 * 		"player-uuid": {
 * 			"label": {
 * 				x,
 * 				y,
 * 				z,
 * 			}
 * 		}
 * }
 */

/**
 * Helper functions for interacting with the coordonates HashMap
*/
public class LocationMap {
	/**
	 * JSON File name
	 */
	private static final String FILE_NAME = "coordinates.json";

	/**
	 * In Memory Map with coordonates
	 */
	private static HashMap<String, HashMap<String, PlayerLocation>> map;
	
	/**
	 * Adds a location to the map
	 * @param uuid UUID of the player
	 * @param label	Label of the place
	 * @param location Location of the player
	 */
	public static void addLocation(String uuid, String label, PlayerLocation location) {
		// If player has no saved coordonates
		if (!map.containsKey(uuid)) {
			// Creates a HashMap for the player
			map.put(uuid, new HashMap<>());
		}

		// Adds the location to the Map
		map.get(uuid).put(label, location);
	}
	
	/**
	 * Retirieves a HashMap of a player by UUID, or an empty one if the player has no saved coordinates
	 * @param uuid UUID of the player
	 * @return A HashMap of the player's saved coordonates, or an empty one if the player has none
	 */
	public static HashMap<String, PlayerLocation> getLocation(String uuid) {
		return map.containsKey(uuid) ? map.get(uuid) : new HashMap<>();
	}

	/**
	 * Writes HashMap to File
	 * @throws IOException
	 */
	public static void DumpToFile() throws IOException {
		// JSON String Builder
		GsonBuilder gb = new GsonBuilder();
		gb.setPrettyPrinting();
		Gson gson = gb.create();

		// Serialize HashMap
		String data = gson.toJson(map);

		// Writes to file and closes it
		FileWriter fw = new FileWriter(FILE_NAME);
		fw.write(data);
		fw.close();
	}

	/**
	 * Loads JSON File into memory HashMap
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void LoadFromFile() throws FileNotFoundException, IOException {
		File f = new File(FILE_NAME);

		// If the file doesn't exists, initializes an empty HashMap
		if (!f.exists()) {
			map = new HashMap<>();
			return;
		}

		// Reads file into a StringBuilder
		Scanner sc = new Scanner(f);

		StringBuilder sb = new StringBuilder();
		while (sc.hasNextLine()) {
			sb.append(sc.nextLine() + "\n");
		}
		sc.close();

		// Deserialization
		Gson gson = new Gson();

		// Needed for nested HashMap
        Type type = new TypeToken<HashMap<String, HashMap<String, PlayerLocation>>>() {}.getType();
		map = gson.fromJson(sb.toString(), type);
	}
}
