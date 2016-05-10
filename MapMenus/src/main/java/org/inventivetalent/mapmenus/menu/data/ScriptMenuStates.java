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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Class to store boolean-states specific to keys and players
 */
@EqualsAndHashCode
@ToString
public class ScriptMenuStates implements IStates {

	@SerializedName("storage") @Expose public Map<String, Collection<StateEntry>> stateMap = new HashMap<>();

	@Override
	@Synchronized
	public void put(String key, Player player) {
		put(key, player, -1);
	}

	@Override
	@Synchronized
	public void put(String key, Player player, long ttl) {
		if (key == null || player == null) { return; }

		Collection<StateEntry> collection = stateMap.get(key);
		if (collection == null) { collection = new ArrayList<>(); }
		StateEntry entry = getEntry(key, player);
		if (entry == null) {
			entry = new StateEntry(key, player.getUniqueId());
		}
		entry.setTime(System.currentTimeMillis());
		entry.setTtl(ttl);
		collection.add(entry);
		stateMap.put(key, collection);
	}

	@Override
	@Synchronized
	public boolean toggle(String key, Player player) {
		return toggle(key, player, -1);
	}

	@Override
	@Synchronized
	public boolean toggle(String key, Player player, long ttl) {
		boolean b = get(key, player);
		if (!b) {
			put(key, player, ttl);
		} else {
			delete(key, player);
		}
		return !b;
	}

	@Synchronized
	private StateEntry getEntry(String key, Player player) {
		if (!stateMap.containsKey(key)) { return null; }
		StateEntry entry = null;
		Collection<StateEntry> entries = stateMap.get(key);
		for (Iterator<StateEntry> iterator = entries.iterator(); iterator.hasNext(); ) {
			StateEntry entry0 = iterator.next();
			if (player.getUniqueId().equals(entry0.getPlayer())) {
				if (entry0.getTtl() == -1) {
					entry = entry0;
					break;
				}
				if (System.currentTimeMillis() - entry0.getTime() > entry0.getTtl()) {
					iterator.remove();
				} else {
					entry = entry0;
					break;
				}
			}
		}
		stateMap.put(key, entries);
		return entry;
	}

	@Override
	@Synchronized
	public boolean get(String key, Player player) {
		if (key == null || player == null) { return false; }

		return stateMap.containsKey(key) && getEntry(key, player) != null;
	}

	@Override
	@Synchronized
	public void delete(String key, Player player) {
		if (key == null || player == null) { return; }

		if (!stateMap.containsKey(key)) { return; }
		Collection<StateEntry> entries = stateMap.get(key);
		for (Iterator<StateEntry> iterator = entries.iterator(); iterator.hasNext(); ) {
			StateEntry entry = iterator.next();
			if (player.getUniqueId().equals(entry.getPlayer())) {
				iterator.remove();
			}
		}
		stateMap.put(key, entries);
	}

	@Override
	public void remove(String key, Player player) {
		delete(key, player);
	}

	@Override
	@Synchronized
	public void deleteAll(String key) {
		if (key == null) { return; }
		if (!stateMap.containsKey(key)) { return; }
		stateMap.remove(key);
	}

	@Override
	public void removeAll(String key) {
		deleteAll(key);
	}

	@Override
	@Synchronized
	public void deleteAllExcept(String key, Player... exceptions) {
		if (key == null) { return; }
		if (exceptions == null || exceptions.length == 0) {
			deleteAll(key);
			return;
		}
		if (!stateMap.containsKey(key)) { return; }
		Collection<StateEntry> entries = stateMap.get(key);
		for (Iterator<StateEntry> iterator = entries.iterator(); iterator.hasNext(); ) {
			StateEntry entry = iterator.next();
			for (Player player : exceptions) {
				if (!player.getUniqueId().equals(entry.getPlayer())) {
					iterator.remove();
				}
			}
		}
		stateMap.put(key, entries);
	}

	@Override
	public void removeAllExcept(String key, Player... exceptions) {
		deleteAllExcept(key, exceptions);
	}

	@Data
	@AllArgsConstructor
	@RequiredArgsConstructor
	@EqualsAndHashCode(exclude = {
			"time",
			"ttl" })
	class StateEntry {
		@Expose final String key;
		@Expose final UUID   player;
		@Expose       long   time;
		@Expose       long   ttl;
	}

}
