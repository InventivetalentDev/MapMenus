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

package org.inventivetalent.mapmenus;

import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.mapmanager.MapManagerPlugin;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.mapmenus.component.ComponentScriptManager;
import org.inventivetalent.mapmenus.menu.CursorPosition;
import org.inventivetalent.mapmenus.menu.MenuManager;
import org.inventivetalent.mapmenus.menu.MenuScriptManager;
import org.inventivetalent.mapmenus.menu.ScriptMapMenu;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.scriptconfig.ScriptConfigProvider;

public class MapMenusPlugin extends JavaPlugin implements Listener {

	public static MapMenusPlugin instance;

	public ScriptConfigProvider scriptProvider;
	public MapManager           mapManager;

	public MenuManager menuManager;

	public MenuScriptManager      menuScriptManager;
	public ComponentScriptManager componentScriptManager;

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();
		PluginAnnotations.CONFIG.load(this, this);

		Bukkit.getPluginManager().registerEvents(this, this);

		scriptProvider = ScriptConfigProvider.create(this);
		mapManager = ((MapManagerPlugin) Bukkit.getPluginManager().getPlugin("MapManager")).getMapManager();

		menuManager = new MenuManager(this);

		Bukkit.getPluginManager().registerEvents(new MenuInteractListener(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

		(menuScriptManager = new MenuScriptManager(this)).saveDefaultFiles();
		(componentScriptManager = new ComponentScriptManager(this)).saveDefaultFiles();

		getLogger().fine("Waiting 5 seconds before loading menus...");
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			@Override
			public void run() {
				getLogger().info("Loading menus...");
				menuManager.readMenusFromFile();
				getLogger().info("Loaded " + menuManager.size() + " menus.");
			}
		}, 100);
	}

	@Override
	public void onDisable() {
		getLogger().info("Saving " + menuManager.size() + " menus...");
		menuManager.writeMenusToFile();
		getLogger().info("Saved.");
	}

	ItemFrame firstFrame;
	ItemFrame secondFrame;

	@EventHandler
	public void on(final PlayerInteractEntityEvent event) {
		if (!event.getPlayer().isSneaking()) { return; }
		final String name = "testMenu" + System.currentTimeMillis();

		if (event.getHand() != EquipmentSlot.HAND) { return; }
		//		menuManager.menuMap.clear();

		if (firstFrame == null) {
			firstFrame = (ItemFrame) event.getRightClicked();
			event.getPlayer().sendMessage("> set #1");
			return;
		}
		if (secondFrame == null) {
			secondFrame = (ItemFrame) event.getRightClicked();
			event.getPlayer().sendMessage("> set #2");
			event.getPlayer().sendMessage("generating...");
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				@Override
				public void run() {
					ScriptMapMenu mapMenu = menuManager.addMenu(name, firstFrame, secondFrame, "ExampleMenu");

					System.out.println(new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(menuManager));

					CursorPosition cursorPosition = CursorPosition.calculateFor(event.getPlayer(), mapMenu);
					System.out.println(cursorPosition);
				}
			}, 20);
			return;
		}

		firstFrame = null;
		secondFrame = null;

		event.getPlayer().sendMessage("> reset");
	}

	@EventHandler
	public void on(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		//		CursorPosition cursorPosition = CursorPosition.calculateFor(player);
		//		System.out.println(cursorPosition);
	}

}
