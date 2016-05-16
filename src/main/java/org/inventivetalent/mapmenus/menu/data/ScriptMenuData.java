/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.mapmenus.menu.data;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.*;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmenus.MapMenusPlugin;

import java.io.IOException;
import java.util.*;

/**
 * Class to store data for menus & components
 */
@EqualsAndHashCode
@ToString
public class ScriptMenuData implements IData {

	public static final TypeAdapter<DataEntry> JSON_ADAPTER = new TypeAdapter<DataEntry>() {

		@Override
		public void write(JsonWriter out, DataEntry entry) throws IOException {
			new Gson().toJson(entry, entry.getClass(), out);
		}

		@Override
		public DataEntry read(JsonReader in) throws IOException {
			JsonObject jsonObject = (JsonObject) new JsonParser().parse(in);
			try {
				return(DataEntry) new Gson().fromJson(jsonObject, Class.forName(jsonObject.get("_type").getAsString()));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	};

	@Data
	@EqualsAndHashCode(exclude = {
			"time",
			"ttl" })
	@ToString
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DataEntry {
		@Expose final String _type = getClass().getName();

		@Expose String key;
		@Expose Object value;
		@Expose long   time;
		@Expose long   ttl;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PlayerDataEntry extends DataEntry {
		@Expose UUID player;
	}

	@Data
	@EqualsAndHashCode(callSuper = true,
					   exclude = "values")
	@ToString(callSuper = true)
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ArrayDataEntry extends DataEntry {
		@Expose List<DataEntry> values = new ArrayList<>();

		@Synchronized
		DataEntry getEntry(int index) {
			if (index < 0) { return null; }
			if (index >= values.size()) { return null; }

			DataEntry entry = values.get(index);
			if (entry == null) { return null; }
			if (entry.getTtl() == -1) { return entry; }
			if (System.currentTimeMillis() - entry.getTime() > entry.getTtl()) {
				values.remove(index);
				return null;
			}
			return entry;
		}
	}

	//	@Expose public JsonObject storage = new JsonObject();
	@Expose public Map<String, DataEntry> storage = new HashMap<>();

	String getPlayerKey(Player player, String key) {
		return "__player" + player.getUniqueId() + "__" + key;
	}

	//	String getArrayKey(String key) {
	//		return "__array__" + key;
	//	}

	@Synchronized
	DataEntry getOrCreateEntry(String key) {
		DataEntry entry = storage.get(key);
		if (entry == null) {
			entry = new DataEntry();
			entry.setKey(key);
		}
		return entry;
	}

	@Synchronized
	PlayerDataEntry getOrCreatePlayerEntry(String key, Player player) {
		DataEntry entry = storage.get(getPlayerKey(player, key));
		if (entry == null) {
			entry = new PlayerDataEntry();
			entry.setKey(key);
		}
		if (!(entry instanceof PlayerDataEntry)) { throw new IllegalStateException("entry is not a player entry"); }
		return (PlayerDataEntry) entry;
	}

	@Synchronized
	DataEntry getEntry(String key) {
		DataEntry entry = storage.get(key);
		if (entry == null) { return null; }
		if (entry.getTtl() == -1) { return entry; }
		if (System.currentTimeMillis() - entry.getTime() > entry.getTtl()) {
			storage.remove(key);
			return null;
		}
		return entry;
	}

	@Synchronized
	ArrayDataEntry getOrCreateArrayEntry(String key) {
		DataEntry entry = storage.get(/*getArrayKey(key)*/key);
		if (entry == null) {
			entry = new ArrayDataEntry();
			entry.setKey(key);
		}
		if (!(entry instanceof ArrayDataEntry)) { throw new IllegalStateException("entry is not an array entry"); }
		return (ArrayDataEntry) entry;
	}

	@Override
	@Synchronized
	public void put(String key, Object value) {
		if (value == null) {
			storage.remove(key);
		} else {
			//			addObjectToJson(storage, key, value);
			DataEntry entry = getOrCreateEntry(key);
			entry.setValue(value);
			entry.setTtl(-1);
			entry.setTime(0);
			storage.put(key, entry);
		}
	}

	@Override
	@Synchronized
	public void put(String key, Object value, long ttl) {
		if (value == null) {
			storage.remove(key);
		} else {
			DataEntry entry = getOrCreateEntry(key);
			entry.setValue(value);
			entry.setTtl(ttl);
			entry.setTime(System.currentTimeMillis());
			storage.put(key, entry);
		}
	}

	@Override
	@Synchronized
	public void put(String key, Player player, Object value, long ttl) {
		if (key == null || player == null) { return; }
		if (value instanceof Boolean) {
			MapMenusPlugin.instance.getLogger().warning("ScriptMenuStates ('states') should be used for boolean values");
		}

		//		JsonObject playerStorage = getPlayerStorage(player);
		//		JsonElement entryElement = playerStorage.get(key);
		//		if (entryElement == null) {
		//			entryElement = new JsonObject();
		//		}
		//		JsonObject entryObject = (JsonObject) entryElement;
		//
		//		addObjectToJson(entryObject, "value", value);
		//		entryObject.addProperty("ttl", ttl);
		//		entryObject.addProperty("time", System.currentTimeMillis());
		//
		//		playerStorage.add(key, entryObject);
		//		storage.add(getPlayerKey(player), playerStorage);

		PlayerDataEntry entry = getOrCreatePlayerEntry(key, player);
		entry.setValue(value);
		entry.setTtl(ttl);
		entry.setTime(System.currentTimeMillis());
		storage.put(entry.getKey(), entry);
	}

	@Override
	@Synchronized
	public void put(String key, Player player, Object value) {
		put(key, player, value, -1);
	}

	@Synchronized
	public void putArray(String key, Object value, long ttl) {
		ArrayDataEntry entry = getOrCreateArrayEntry(key);

		DataEntry newEntry = new DataEntry(key + "-" + entry.values.size(), value, ttl != -1 ? System.currentTimeMillis() : 0, ttl);
		entry.values.add(newEntry);

		storage.put(entry.getKey(), entry);
	}

	@Synchronized
	public void putArray(String key, Object value) {
		putArray(key, value, -1);
	}

	@Synchronized
	public Object getArray(String key, int index) {
		DataEntry entry = storage.get(key);
		if (entry == null) { return null; }
		if (!(entry instanceof ArrayDataEntry)) { return null; }
		if (((ArrayDataEntry) entry).values.size() <= index) { return null; }
		return ((ArrayDataEntry) entry).getEntry(index).value;
	}

	@Synchronized
	public Object[] getArray(String key) {
		DataEntry entry = storage.get(key);
		if (entry == null) { return null; }
		if (!(entry instanceof ArrayDataEntry)) { return null; }
		// First iteration to remove expired elements
		Object[] objects = new Object[((ArrayDataEntry) entry).values.size()];
		for (int i = 0; i < objects.length; i++) {
			objects[i] = ((ArrayDataEntry) entry).getEntry(i);
		}

		// Actual iteration after all null-elements are removed
		objects = new Object[((ArrayDataEntry) entry).values.size()];
		for (int i = 0; i < objects.length; i++) {
			DataEntry entry1 = ((ArrayDataEntry) entry).getEntry(i);
			objects[i] = entry1 != null ? entry1.value : null;
		}

		return objects;
	}

	@Synchronized
	public void deleteArray(String key, int index) {
		DataEntry entry = storage.get(key);
		if (entry == null) { return; }
		if (!(entry instanceof ArrayDataEntry)) { return; }
		if (((ArrayDataEntry) entry).values.size() <= index) { return; }
		((ArrayDataEntry) entry).values.remove(index);
		storage.put(key, entry);
	}

	@Synchronized
	public void removeArray(String key, int index) {
		deleteArray(key, index);
	}

	//	@Synchronized
	//	JsonObject getPlayerStorage(Player player) {
	//		String objectKey = getPlayerKey(player);
	//		JsonElement jsonElement = storage.get(objectKey);
	//		if (jsonElement == null) {
	//			jsonElement = new JsonObject();
	//			storage.add(objectKey, jsonElement);
	//		}
	//		return (JsonObject) jsonElement;
	//	}

	@Override
	@Synchronized
	public void delete(String key) {
		storage.remove(key);
	}

	@Override
	public void remove(String key) {
		delete(key);
	}

	@Override
	@Synchronized
	public void delete(String key, Player player) {
		//		JsonObject playerStorage = getPlayerStorage(player);
		//		playerStorage.remove(key);
		//
		//		storage.add(getPlayerKey(player), playerStorage);
		storage.remove(getPlayerKey(player, key));
	}

	@Override
	public void remove(String key, Player player) {
		delete(key, player);
	}

	@Override
	@Synchronized
	public Object get(String key) {
		//		JsonElement jsonElement = storage.get(key);
		//		return parseFromJson(jsonElement);
		DataEntry entry = getEntry(key);
		if (entry == null) { return null; }
		return entry.getValue();
	}

	@Override
	@Synchronized
	public Object get(String key, Player player) {
		//		JsonObject jsonObject = getPlayerStorage(player);
		//		JsonElement entryElement = jsonObject.get(key);
		//		if (entryElement == null) { return null; }
		//		JsonObject entryObject = (JsonObject) entryElement;
		//
		//		if (entryObject.get("ttl").getAsLong() == -1) { return parseFromJson(entryObject.get("value")); }
		//		if (System.currentTimeMillis() - entryObject.get("time").getAsLong() > entryObject.get("ttl").getAsLong()) {
		//			jsonObject.remove(key);
		//			return null;
		//		} else {
		//			return parseFromJson(entryObject.get("value"));
		//		}
		DataEntry entry = getEntry(getPlayerKey(player, key));
		if (entry == null) { return null; }
		if (!(entry instanceof PlayerDataEntry)) { return null; }
		return entry;
	}

	@Override
	@Synchronized
	public boolean has(String key) {
		return storage.containsKey(key) && get(key) != null;
	}

	@Override
	@Synchronized
	public boolean has(String key, Player player) {
		return storage.containsKey(getPlayerKey(player, key)) && get(key, player) != null;
	}

	void addObjectToJson(JsonObject jsonObject, String key, Object value) {
		if (value instanceof String) {
			jsonObject.addProperty(key, (String) value);
		} else if (value instanceof Number) {
			jsonObject.addProperty(key, (Number) value);
		} else if (value instanceof Boolean) {
			jsonObject.addProperty(key, (Boolean) value);
		} else if (value instanceof Character) {
			jsonObject.addProperty(key, (Character) value);
		} else {
			JsonObject classObject = new JsonObject();
			classObject.addProperty("__class", value.getClass().getName());
			classObject.add("__value", new Gson().toJsonTree(value));
			jsonObject.add(key, /*new JsonParser().parse(value.toString())*/classObject);
		}
	}

	Object parseFromJson(JsonElement jsonElement) {
		if (jsonElement == null || jsonElement.isJsonNull()) {
			return null;
		} else {
			if (jsonElement.isJsonPrimitive()) {
				JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
				if (jsonPrimitive.isString()) {
					return jsonPrimitive.getAsString();
				} else if (jsonPrimitive.isNumber()) {
					return jsonPrimitive.getAsNumber();
				} else if (jsonPrimitive.isBoolean()) {
					return jsonPrimitive.getAsBoolean();
				} else {
					return jsonPrimitive.toString();
				}
			} else if (jsonElement.isJsonObject()) {
				JsonObject object = jsonElement.getAsJsonObject();
				if (object.has("__class")) {
					try {
						Class clazz = Class.forName(object.get("__class").getAsString());
						return new Gson().fromJson(object.getAsJsonObject("__value"), clazz);
					} catch (ClassNotFoundException ignored) {
					}
				}
			}
		}
		return new Gson().toJson(jsonElement);
	}
}
