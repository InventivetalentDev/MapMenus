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

package org.inventivetalent.mapmenus.menu;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Class to store data for menus & components
 */
@EqualsAndHashCode
@ToString
public class ScriptMenuData {

	@Expose public JsonObject storage;

	public ScriptMenuData(JsonObject storage) {
		this.storage = storage;
	}

	public void put(String key, Object value) {
		if (value == null) {
			storage.remove(key);
		} else {
			if (value instanceof String) {
				storage.addProperty(key, (String) value);
			} else if (value instanceof Number) {
				storage.addProperty(key, (Number) value);
			} else if (value instanceof Boolean) {
				storage.addProperty(key, (Boolean) value);
			} else if (value instanceof Character) {
				storage.addProperty(key, (Character) value);
			} else {
				storage.add(key, new JsonParser().parse(value.toString()));
			}
		}
	}

	public void delete(String key) {
		storage.remove(key);
	}

	public Object get(String key) {
		JsonElement jsonElement = storage.get(key);
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
			}
		}
		return new Gson().toJson(jsonElement);
	}
}
