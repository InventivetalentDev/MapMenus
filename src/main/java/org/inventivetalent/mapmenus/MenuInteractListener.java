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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.inventivetalent.mapmanager.TimingsHelper;
import org.inventivetalent.mapmanager.event.MapInteractEvent;
import org.inventivetalent.mapmenus.menu.CursorPosition;
import org.inventivetalent.mapmenus.menu.ScriptMapMenu;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.vectors.d3.Vector3DDouble;

import java.util.Set;

public class MenuInteractListener implements Listener {

	private MapMenusPlugin plugin;

	public MenuInteractListener(MapMenusPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void on(final MapInteractEvent event) {
		if (event.getItemFrame().hasMetadata("MAP_MENUS_META")) {
			event.setCancelled(true);
		}
		Bukkit.getScheduler().runTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (event.getActionID() == 2) {// interact_at
					return;
				}
				if (event.getHandID() != 0) { return; }

				handleInteract(event.getPlayer(), event, event.getActionID());
			}
		});
	}

	@EventHandler
	public void on(PlayerInteractEvent event) {
		if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1)) {
			if (event.getHand() != EquipmentSlot.HAND) { return; }
		}

		int actionId = 0;
		switch (event.getAction()) {
			case RIGHT_CLICK_AIR:
			case RIGHT_CLICK_BLOCK:
				actionId = 0;
				break;
			case LEFT_CLICK_AIR:
			case LEFT_CLICK_BLOCK:
				actionId = 1;
				break;
		}
		handleInteract(event.getPlayer(), event, actionId);
	}

	void handleInteract(final Player player, Cancellable cancellable, final int action/* 0 = interact (right-click), 1 = attack (left-click) */) {
		TimingsHelper.startTiming("MapMenu:handleInteract");

		Block targetBlock = player.getTargetBlock((Set<Material>) null, 16);
		if (targetBlock != null && targetBlock.getType() != Material.AIR) {
			TimingsHelper.startTiming("MapMenu:handleInteract:getMenusOnBlock");
			Set<ScriptMapMenu> menus = plugin.menuManager.getMenusOnBlock(new Vector3DDouble(targetBlock.getLocation()));
			TimingsHelper.stopTiming("MapMenu:handleInteract:getMenusOnBlock");

			TimingsHelper.startTiming("MapMenu:handleInteract:findMenuByCursor");
			final CursorPosition.CursorMenuQueryResult queryResult = CursorPosition.findMenuByCursor(player, menus);
			TimingsHelper.stopTiming("MapMenu:handleInteract:findMenuByCursor");

			if (queryResult != null && queryResult.isFound()) {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					@Override
					public void run() {
						TimingsHelper.startTiming("MapMenu:handleInteract:click");
						boolean clickHandled = queryResult.getMenu().click(player, queryResult.getPosition(), action);
						TimingsHelper.stopTiming("MapMenu:handleInteract:click");
					}
				});

				cancellable.setCancelled(true);
			}
		}

		TimingsHelper.stopTiming("MapMenu:handleInteract");
	}

}
