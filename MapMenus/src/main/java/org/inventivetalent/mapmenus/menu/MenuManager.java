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
import lombok.Synchronized;
import lombok.ToString;
import org.bukkit.entity.ItemFrame;
import org.inventivetalent.mapmenus.MapMenusPlugin;
import org.inventivetalent.mapmenus.TimingsHelper;
import org.inventivetalent.vectors.d3.Vector3DDouble;

import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

@EqualsAndHashCode
@ToString
public class MenuManager {

	public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	private MapMenusPlugin plugin;

	private File saveDirectory;
	private File indexFile;

	@Expose @SerializedName("menus") private Map<String, ScriptMapMenu> menuMap = new HashMap<>();

	public MenuManager(MapMenusPlugin plugin) {
		this.plugin = plugin;

		try {
			this.saveDirectory = new File(new File(plugin.getDataFolder(), "saves"), "menus");
			if (!this.saveDirectory.exists()) { this.saveDirectory.mkdirs(); }
			this.indexFile = new File(this.saveDirectory, "index.mmi");
			if (!this.indexFile.exists()) { this.indexFile.createNewFile(); }
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Synchronized
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

	@Synchronized
	public void removeMenu(ScriptMapMenu menu) {
		if (menu != null) {
			menuMap.remove(menu.getName());
		}
	}

	@Synchronized
	public ScriptMapMenu removeMenu(String name) {
		ScriptMapMenu menu = menuMap.remove(name);
		if (menu != null) {
			menu.dispose();
		}
		return menu;
	}

	@Synchronized
	public ScriptMapMenu getMenuForBoundingBoxVector(Vector3DDouble vector) {
		for (ScriptMapMenu mapMenu : getMenus()) {
			if (mapMenu.getBoundingBox().contains(vector)) {
				return mapMenu;
			}
		}
		return null;
	}

	// Should in theory only return a maximum of 4 menus
	@Synchronized
	public Set<ScriptMapMenu> getMenusOnBlock(Vector3DDouble block) {
		Set<ScriptMapMenu> menus = new HashSet<>();
		for (ScriptMapMenu mapMenu : getMenus()) {
			if (mapMenu.isOnBlock(block)) {
				menus.add(mapMenu);
			}
		}
		return menus;
	}

	@Synchronized
	public Set<String> getMenuNames() {
		return new HashSet<>(menuMap.keySet());
	}

	@Synchronized
	public Set<ScriptMapMenu> getMenus() {
		return new HashSet<>(menuMap.values());
	}

	@Synchronized
	public Set<ScriptMapMenu> getMenusInWorld(String worldName) {
		Set<ScriptMapMenu> frames = new HashSet<>();
		for (ScriptMapMenu menu : menuMap.values()) {
			if (menu.getWorldName().equals(worldName)) {
				frames.add(menu);
			}
		}
		return frames;
	}

	@Synchronized
	public int size() {
		return menuMap.size();
	}

	@Synchronized
	public void writeMenusToFile() {
		TimingsHelper.startTiming("MapMenu - writeToFile");

		for (ScriptMapMenu menu : getMenus()) {
			plugin.getLogger().fine("Saving '" + menu.getName() + "' in world '" + menu.getWorld().getName() + "'...");
			try {
				File saveFile = new File(saveDirectory, URLEncoder.encode(menu.getName(), "UTF-8") + ".mmd");
				if (!saveFile.exists()) { saveFile.createNewFile(); }
				try (Writer writer = new FileWriter(saveFile)) {
					GSON.toJson(menu, writer);
				}
			} catch (IOException e) {
				plugin.getLogger().log(Level.WARNING, "Failed to save Menu '" + menu.getName() + "'", e);
			}
		}

		try {
			try (Writer writer = new FileWriter(indexFile)) {
				new Gson().toJson(getMenuNames(), writer);
			}
		} catch (IOException e) {
			plugin.getLogger().log(Level.WARNING, "Failed to save Menu-Index file", e);
		}

		TimingsHelper.stopTiming("MapMenu - writeToFile");
	}

	@Synchronized
	public void readMenusFromFile() {
		TimingsHelper.startTiming("MapMenu - readFromFile");

		Set<String> index;
		try {
			try (Reader reader = new FileReader(indexFile)) {
				index = (Set<String>) new Gson().fromJson(reader, HashSet.class);
			}
		} catch (IOException e) {
			TimingsHelper.stopTiming("MapMenu - readFromFile");
			throw new RuntimeException("Failed to load Menu-Index file", e);
		}
		if (index == null) {
			plugin.getLogger().info("No index found > First time startup or data deleted");
			return;
		}

		for (String name : index) {
			try {
				File file = new File(saveDirectory, URLEncoder.encode(name, "UTF-8") + ".mmd");
				try (Reader reader = new FileReader(file)) {
					ScriptMapMenu loadedMenu = GSON.fromJson(reader, ScriptMapMenu.class);
					menuMap.put(loadedMenu.getName(), loadedMenu);
					loadedMenu.initRenderer();
					loadedMenu.reloadScript();
				}
			} catch (IOException e) {
				plugin.getLogger().log(Level.WARNING, "Failed to load Menu '" + name + "'", e);
			}
		}

		TimingsHelper.stopTiming("MapMenu - readFromFile");
	}

}
