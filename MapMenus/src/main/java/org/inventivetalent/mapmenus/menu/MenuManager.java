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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.entity.ItemFrame;
import org.inventivetalent.mapmenus.MapMenusPlugin;
import org.inventivetalent.vectors.d3.Vector3DDouble;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode
@ToString
public class MenuManager {

	public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	private MapMenusPlugin plugin;

	@Expose @SerializedName("menus") public Map<String, ScriptMapMenu> menuMap = new HashMap<>();

	public MenuManager(MapMenusPlugin plugin) {
		this.plugin = plugin;
	}

	public ScriptMapMenu addMenu(String name, ItemFrame itemFrameA, ItemFrame itemFrameB, String script) {
		if (menuMap.containsKey(name)) {
			throw new IllegalArgumentException("Menu '" + name + "' already exists");
		}
		if (!plugin.menuScriptManager.doesScriptExist(script)) {
			plugin.getLogger().warning("Menu Script '" + script + "' does not exist");
			return null;
		}

		ScriptMapMenu mapMenu = new ScriptMapMenu(itemFrameA, new Vector3DDouble(itemFrameA.getLocation().toVector()), new Vector3DDouble(itemFrameB.getLocation().toVector()), name);
		menuMap.put(mapMenu.getName(), mapMenu);
		mapMenu.setScript(script);
		mapMenu.reloadScript();

		return mapMenu;
	}

	public ScriptMapMenu getMenuForBoundingBoxVector(Vector3DDouble vector) {
		for (ScriptMapMenu mapMenu : getMenus()) {
			if (mapMenu.boundingBox.contains(vector)) {
				return mapMenu;
			}
		}
		return null;
	}

	// Should in theory only return a maximum of 4 menus
	public Set<ScriptMapMenu> getMenusOnBlock(Vector3DDouble block) {
		Set<ScriptMapMenu> menus = new HashSet<>();
		for (ScriptMapMenu mapMenu : getMenus()) {
			if (mapMenu.isOnBlock(block)) {
				menus.add(mapMenu);
			}
		}
		return menus;
	}

	public Set<String> getMenuNames() {
		return new HashSet<>(menuMap.keySet());
	}

	public Set<ScriptMapMenu> getMenus() {
		return new HashSet<>(menuMap.values());
	}
}
