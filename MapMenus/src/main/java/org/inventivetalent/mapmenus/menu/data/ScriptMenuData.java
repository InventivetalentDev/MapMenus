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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import lombok.*;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmenus.MapMenusPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class to store data for menus & components
 */
@EqualsAndHashCode
@ToString
public class ScriptMenuData implements IData {

	@Data
	@EqualsAndHashCode(exclude = {
			"time",
			"ttl" })
	@ToString
	@AllArgsConstructor
	@NoArgsConstructor
	static class DataEntry {
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
	static class PlayerDataEntry extends DataEntry {
		@Expose UUID player;
	}

	//	@Expose public JsonObject storage = new JsonObject();
	@Expose public Map<String, DataEntry> storage = new HashMap<>();

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

	String getPlayerKey(Player player, String key) {
		return "__player" + player.getUniqueId() + "__" + key;
	}

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
		return storage.containsKey(key);
	}

	@Override
	@Synchronized
	public boolean has(String key, Player player) {
		return storage.containsKey(getPlayerKey(player, key));
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
