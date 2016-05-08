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

import org.bukkit.entity.Player;

public class MapperStates implements IStates {

	private final IStates parentStates;
	private final String  keyFormat;

	public MapperStates(IStates parentStates, String keyFormat) {
		this.parentStates = parentStates;
		this.keyFormat = keyFormat;
	}

	String formatKey(String key) {
		return String.format(keyFormat, key);
	}

	@Override
	public void put(String key, Player player, long ttl) {
		parentStates.put(formatKey(key), player, ttl);
	}

	@Override
	public void put(String key, Player player) {
		parentStates.put(formatKey(key), player);
	}

	@Override
	public boolean toggle(String key, Player player, long ttl) {
		return parentStates.toggle(formatKey(key), player, ttl);
	}

	@Override
	public boolean toggle(String key, Player player) {
		return parentStates.toggle(formatKey(key), player);
	}

	@Override
	public boolean get(String key, Player player) {
		return parentStates.get(formatKey(key), player);
	}

	@Override
	public void delete(String key, Player player) {
		parentStates.delete(formatKey(key), player);
	}

	@Override
	public void deleteAll(String key) {
		parentStates.deleteAll(key);
	}

	@Override
	public void deleteAllExcept(String key, Player... exceptions) {
		parentStates.deleteAllExcept(key, exceptions);
	}
}
