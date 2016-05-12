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

package org.inventivetalent.mapmenus.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.inventivetalent.eventcallbacks.PlayerEventCallback;
import org.inventivetalent.mapmenus.MapMenusPlugin;
import org.inventivetalent.mapmenus.MenuScriptException;
import org.inventivetalent.mapmenus.menu.ScriptMapMenu;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.pluginannotations.command.Command;
import org.inventivetalent.pluginannotations.command.Completion;
import org.inventivetalent.pluginannotations.command.OptionalArg;
import org.inventivetalent.pluginannotations.command.Permission;
import org.inventivetalent.pluginannotations.message.MessageFormatter;
import org.inventivetalent.pluginannotations.message.MessageLoader;
import org.inventivetalent.scriptconfig.InvalidScriptException;
import org.inventivetalent.vectors.d3.Vector3DDouble;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class MenuCommands {

	static final MessageLoader MESSAGE_LOADER = PluginAnnotations.MESSAGE.newMessageLoader(Bukkit.getPluginManager().getPlugin("MapMenus"), "config.yml", "message.command", null);

	private MapMenusPlugin plugin;

	public MenuCommands(MapMenusPlugin plugin) {
		this.plugin = plugin;
	}

	@Command(name = "mapmenus",
			 aliases = {
					 "mapmenuhelp" },
			 usage = "",
			 max = 0,
			 fallbackPrefix = "mapmenus")
	public void helpCommand(final CommandSender sender) {
		sender.sendMessage("§6MapMenus v" + plugin.getDescription().getVersion() + (plugin.updateAvailable ? " §a*Update available" : ""));
		sender.sendMessage(" ");

		int c = 0;
		if (sender.hasPermission("mapmenus.create")) {
			c++;
			sender.sendMessage("§e/mmc <Name> <Script>");
			sender.sendMessage("§bCreate a menu");
			sender.sendMessage(" ");
		}
		if (sender.hasPermission("mapmenus.remove")) {
			c++;
			sender.sendMessage("§e/mmr <Name>");
			sender.sendMessage("§bRemove a menu");
			sender.sendMessage(" ");
		}
		if (sender.hasPermission("mapmenus.list")) {
			c++;
			sender.sendMessage("§e/mml [Script]");
			sender.sendMessage("§bShow a list of menus");
			sender.sendMessage(" ");
		}

		if (c > 0) {
			sender.sendMessage("§eType §a/help <Command> §efor more information.");
		}
	}

	@Command(name = "createmenu",
			 aliases = {
					 "createmapmenu",
					 "mapmenucreate",
					 "mmc" },
			 usage = "<Name> <Script>",
			 description = "Create a new menu",
			 min = 2,
			 max = 2,
			 fallbackPrefix = "mapmenus")
	@Permission("mapmenus.create")
	public void createMenu(final Player sender, final String name, final String script) {
		if (plugin.menuManager.doesMenuExist(name)) {
			sender.sendMessage(MESSAGE_LOADER.getMessage("error.create.exists", "error.create.exists"));
			return;
		}
		if (!plugin.menuScriptManager.doesScriptExist(script)) {
			sender.sendMessage(MESSAGE_LOADER.getMessage("error.create.unknownScript", "error.create.unknownScript"));
			return;
		}

		sender.sendMessage("  ");
		sender.sendMessage(MESSAGE_LOADER.getMessage("create.setup.first", "create.setup.first"));
		plugin.eventCallbacks.listenFor(PlayerInteractEntityEvent.class, new PlayerEventCallback<PlayerInteractEntityEvent>(sender) {
			@Override
			public void callPlayer(PlayerInteractEntityEvent event) {
				if (event != null && event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
					final ItemFrame firstFrame = (ItemFrame) event.getRightClicked();
					sender.sendMessage(MESSAGE_LOADER.getMessage("create.setup.set.first", "create.setup.set.first"));
					sender.sendMessage("  ");

					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							sender.sendMessage(MESSAGE_LOADER.getMessage("create.setup.second", "create.setup.second"));
							plugin.eventCallbacks.listenFor(PlayerInteractEntityEvent.class, new PlayerEventCallback<PlayerInteractEntityEvent>(sender) {
								@Override
								public void callPlayer(PlayerInteractEntityEvent event) {
									if (event != null && event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
										final ItemFrame secondFrame = (ItemFrame) event.getRightClicked();
										sender.sendMessage(MESSAGE_LOADER.getMessage("create.setup.set.second", "create.setup.set.second"));
										sender.sendMessage("  ");

										sender.sendMessage(MESSAGE_LOADER.getMessage("create.setup.complete", "create.setup.complete", new MessageFormatter() {
											@Override
											public String format(String key, String message) {
												return String.format(message, name, script);
											}
										}));

										try {
											ScriptMapMenu menu = plugin.menuManager.createMenu(name, firstFrame, secondFrame, script);
											sender.sendMessage(MESSAGE_LOADER.getMessage("create.setup.created", "create.setup.created"));
										} catch (MenuScriptException e) {
											sender.sendMessage("§c" + e.getMessage());
											plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
										}
									}
								}
							});
						}
					}, 10);
				}
			}
		});
	}

	@Completion(name = "createmenu")
	public void createMenu(final List<String> completions, final Player sender, final String name, final String script) {
		if (name != null) {
			completions.addAll(plugin.menuScriptManager.getScripts());
		}
	}

	@Command(name = "removemenu",
			 aliases = {
					 "removemapmenu",
					 "mapmenuremove",
					 "mmr" },
			 usage = "<Name>",
			 min = 1,
			 max = 1,
			 fallbackPrefix = "mapmenus")
	@Permission("mapmenus.remove")
	public void removeMenu(final Player sender, final String name) {
		if (!plugin.menuManager.doesMenuExist(name)) {
			sender.sendMessage(MESSAGE_LOADER.getMessage("error.remove.notFound", "error.remove.notFound"));
			return;
		}
		final ScriptMapMenu menu = plugin.menuManager.removeMenu(name);
		menu.dispose();
		menu.renderer.dispose();
		sender.sendMessage(MESSAGE_LOADER.getMessage("remove.removed", "remove.removed"));
	}

	@Completion(name = "removemenu")
	public void removeMenu(final List<String> completions, final CommandSender sender, final String name) {
		for (ScriptMapMenu menu : plugin.menuManager.getMenus()) {
			completions.add(menu.getName());
		}
	}

	@Command(name = "listmenus",
			 aliases = {
					 "listmapmenus",
					 "mapmenulist",
					 "mml" },
			 usage = "[Script RegEx]",
			 min = 0,
			 max = 1,
			 fallbackPrefix = "mapmenus")
	@Permission("mapmenus.list")
	public void listMenus(final Player sender, @OptionalArg(def = ".*") final String scriptRegex) {
		Pattern scriptPattern = Pattern.compile(scriptRegex);
		sender.sendMessage("  ");

		Set<ScriptMapMenu> menus = plugin.menuManager.getMenus();
		sender.sendMessage("§eMenus (" + menus.size() + ")");
		for (ScriptMapMenu menu : menus) {
			if (scriptPattern.matcher(menu.getScriptName()).matches()) {
				TextComponent component = new TextComponent(menu.getName());
				component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { new TextComponent("§8Script: §7" + menu.getScriptName()) }));

				Vector3DDouble teleportVector = menu.getBaseVector();
				ClickEvent teleportClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + teleportVector.getX() + " " + teleportVector.getY() + " " + teleportVector.getZ());
				HoverEvent teleportHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport to " + teleportVector.getX() + "," + teleportVector.getY() + "," + teleportVector.getZ()).color(ChatColor.GRAY).create());
				component.addExtra(new ComponentBuilder(" [Teleport]").color(ChatColor.YELLOW).bold(true).event(teleportClick).event(teleportHover).create()[0]);

				ClickEvent deleteClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mapmenus:removemenu " + menu.getName());
				component.addExtra(new ComponentBuilder(" [Delete]").color(ChatColor.RED).bold(true).event(deleteClick).create()[0]);

				sender.spigot().sendMessage(component);
			}
		}
	}

	@Command(name = "reloadmenu",
			 aliases = {
					 "reloadmapmenus",
					 "mapmenureload",
					 "mmrl" },
			 usage = "<Name>",
			 min = 1,
			 max = 1,
			 fallbackPrefix = "mapmenus")
	@Permission("mapmenus.reloadmenu")
	public void reloadMenu(final CommandSender sender, final String name) {
		if (!plugin.menuManager.doesMenuExist(name)) {
			sender.sendMessage(MESSAGE_LOADER.getMessage("error.remove.notFound", "error.remove.notFound"));
			return;
		}
		final ScriptMapMenu menu = plugin.menuManager.getMenu(name);
		// Dispose existing components
		for (UUID componentId : menu.getComponentIds()) {
			menu.getComponent(componentId).dispose();
		}

		menu.renderer.dispose();

		menu.initRenderer();
		try {
			menu.reloadScript();
			sender.sendMessage(MESSAGE_LOADER.getMessage("reload.reloaded", "reload.reloaded"));
		} catch (InvalidScriptException e) {
			MenuScriptException exception = new MenuScriptException("Invalid script in file " + e.getScriptSource() + ", line " + e.getScriptException().getLineNumber() + ":" + e.getScriptException().getColumnNumber());
			sender.sendMessage("§c" + exception.getMessage());
			plugin.getLogger().log(Level.WARNING, exception.getMessage(), exception);
		}
	}

	@Completion(name = "reloadmenu")
	public void reloadMenu(final List<String> completions, final CommandSender sender, final String name) {
		for (ScriptMapMenu menu : plugin.menuManager.getMenus()) {
			completions.add(menu.getName());
		}
	}

}
