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

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.eventcallbacks.EventCallbacks;
import org.inventivetalent.mapmanager.MapManagerPlugin;
import org.inventivetalent.mapmanager.manager.MapManager;
import org.inventivetalent.mapmenus.command.MenuCommands;
import org.inventivetalent.mapmenus.component.ComponentScriptManager;
import org.inventivetalent.mapmenus.menu.MenuManager;
import org.inventivetalent.mapmenus.menu.MenuScriptManager;
import org.inventivetalent.mapmenus.provider.MenuProviders;
import org.inventivetalent.mapmenus.provider.internal.PlaceholderProvider;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.pluginannotations.config.ConfigValue;
import org.inventivetalent.scriptconfig.ScriptConfigProvider;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;
import org.mcstats.MetricsLite;

public class MapMenusPlugin extends JavaPlugin implements Listener {

	public static MapMenusPlugin instance;

	public ScriptConfigProvider scriptProvider;
	public MapManager           mapManager;

	public MenuManager menuManager;

	public MenuScriptManager      menuScriptManager;
	public ComponentScriptManager componentScriptManager;

	public MenuProviders menuProviders;

	//	public InputListener inputListener;
	public EventCallbacks eventCallbacks;

	SpigetUpdate spigetUpdate;
	public boolean updateAvailable;

	@ConfigValue(path = "debug.enabled") public   boolean debug          = false;
	@ConfigValue(path = "debug.particles") public boolean debugParticles = false;

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();
		PluginAnnotations.CONFIG.load(this, this);
		PluginAnnotations.COMMAND.load(this, new MenuCommands(this));

		Bukkit.getPluginManager().registerEvents(this, this);

		scriptProvider = ScriptConfigProvider.create(this);
		mapManager = ((MapManagerPlugin) Bukkit.getPluginManager().getPlugin("MapManager")).getMapManager();

		menuManager = new MenuManager(this);

		Bukkit.getPluginManager().registerEvents(new MenuInteractListener(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
		//		Bukkit.getPluginManager().registerEvents(inputListener = new InputListener(this), this);
		this.eventCallbacks = EventCallbacks.of(this);

		(menuScriptManager = new MenuScriptManager(this)).saveDefaultFiles();
		(componentScriptManager = new ComponentScriptManager(this)).saveDefaultFiles();

		menuProviders = new MenuProviders(this);
		// Internal providers
		new PlaceholderProvider().register();
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			getLogger().info("Found PlaceholderAPI!");
			((PlaceholderProvider) menuProviders.get("Placeholders")).addReplacer(new PlaceholderProvider.IPlaceholderReplacer() {
				@Override
				public String replace(Player player, String string) {
					return PlaceholderAPI.setPlaceholders(player, string);
				}
			});
		}

		getLogger().fine("Waiting 5 seconds before loading menus...");
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			@Override
			public void run() {
				getLogger().info("Loading menus...");
				menuManager.readMenusFromFile();
				getLogger().info("Loaded " + menuManager.size() + " menus.");
			}
		}, 100);

		try {
			MetricsLite metrics = new MetricsLite(this);
			if (metrics.start()) {
				getLogger().info("Metrics started");
			}

			spigetUpdate = new SpigetUpdate(this, 3131).setUserAgent("MapMenus/" + getDescription().getVersion()).setVersionComparator(VersionComparator.SEM_VER);
			spigetUpdate.checkForUpdate(new UpdateCallback() {
				@Override
				public void updateAvailable(String s, String s1, boolean b) {
					updateAvailable = true;
					getLogger().info("A new version is available (" + s + "). Download it from https://r.spiget.org/3131");
				}

				@Override
				public void upToDate() {
					getLogger().info("The plugin is up-to-date.");
				}
			});
		} catch (Exception e) {
		}
	}

	@Override
	public void onDisable() {
		getLogger().info("Saving " + menuManager.size() + " menus...");
		menuManager.writeMenusToFile();
		getLogger().info("Saved.");
	}

	@EventHandler
	public void on(final PlayerJoinEvent event) {
		if (event.getPlayer().hasPermission("mapmenus.updatecheck")) {
			spigetUpdate.checkForUpdate(new UpdateCallback() {
				@Override
				public void updateAvailable(String s, String s1, boolean b) {
					updateAvailable = true;
					event.getPlayer().sendMessage("§aA new version for §6MapMenus §ais available (§7v" + s + "§a). Download it from https://r.spiget.org/3131");
				}

				@Override
				public void upToDate() {
				}
			});
		}
	}

}
