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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InputListener implements Listener {

	private final Map<UUID, Callback<AsyncPlayerChatEvent>>     CHAT_MAP            = new ConcurrentHashMap<>();
	private final Map<UUID, Callback<PlayerMoveEvent>>          MOVE_MAP            = new ConcurrentHashMap<>();
	private final Map<UUID, Callback<PlayerInteractEntityEvent>> ENTITY_INTERACT_MAP = new ConcurrentHashMap<>();

	private MapMenusPlugin plugin;

	public InputListener(MapMenusPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void on(AsyncPlayerChatEvent event) {
		Callback<AsyncPlayerChatEvent> callback;
		while ((callback = CHAT_MAP.remove(event.getPlayer().getUniqueId())) != null)
			callback.call(event);
	}

	public void listenForChat(Player player, Callback<AsyncPlayerChatEvent> callback) {
		if (callback == null) { return; }
		if (player != null) {
			CHAT_MAP.put(player.getUniqueId(), callback);
		} else {
			callback.call(null);
		}
	}

	@EventHandler
	public void on(PlayerMoveEvent event) {
		Callback<PlayerMoveEvent> callback;
		while ((callback = MOVE_MAP.remove(event.getPlayer().getUniqueId())) != null)
			callback.call(event);
	}

	public void listenForMove(Player player, Callback<PlayerMoveEvent> callback) {
		if (callback == null) { return; }
		if (player != null) {
			MOVE_MAP.put(player.getUniqueId(), callback);
		} else {
			callback.call(null);
		}
	}

	@EventHandler
	public void on(PlayerInteractEntityEvent event) {
		Callback<PlayerInteractEntityEvent> callback;
		while ((callback = ENTITY_INTERACT_MAP.remove(event.getPlayer().getUniqueId())) != null)
			callback.call(event);
	}

	public void listenForEntityInteract(Player player, Callback<PlayerInteractEntityEvent> callback) {
		if (callback == null) { return; }
		if (player != null) {
			ENTITY_INTERACT_MAP.put(player.getUniqueId(), callback);
		} else {
			callback.call(null);
		}
	}

}
