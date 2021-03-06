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

public class MapperData implements IData {

	private final IData  parentData;
	private final String keyFormat;

	public MapperData(IData parentData, String keyFormat) {
		this.parentData = parentData;
		this.keyFormat = keyFormat;
	}

	String formatKey(String key) {
		return String.format(keyFormat, key);
	}

	@Override
	public void put(String key, Object value) {
		parentData.put(formatKey(key), value);
	}

	@Override
	public void put(String key, Object value, long ttl) {
		parentData.put(formatKey(key), value, ttl);
	}

	@Override
	public void put(String key, Player player, Object value, long ttl) {
		parentData.put(formatKey(key), player, value, ttl);
	}

	@Override
	public void put(String key, Player player, Object value) {
		parentData.put(formatKey(key), player, value);
	}

	@Override
	public void delete(String key) {
		parentData.delete(formatKey(key));
	}

	@Override
	public void remove(String key) {
		parentData.remove(formatKey(key));
	}

	@Override
	public void delete(String key, Player player) {
		parentData.delete(formatKey(key), player);
	}

	@Override
	public void remove(String key, Player player) {
		parentData.remove(formatKey(key), player);
	}

	@Override
	public Object get(String key) {
		return parentData.get(formatKey(key));
	}

	@Override
	public Object get(String key, Player player) {
		return parentData.get(formatKey(key), player);
	}

	@Override
	public boolean has(String key) {
		return parentData.has(formatKey(key));
	}

	@Override
	public boolean has(String key, Player player) {
		return parentData.has(formatKey(key), player);
	}

	@Override
	public void putArray(String key, Object value, long ttl) {
		parentData.putArray(formatKey(key), value, ttl);
	}

	@Override
	public void putArray(String key, Object value) {
		parentData.putArray(formatKey(key), value);
	}

	@Override
	public Object getArray(String key, int index) {
		return parentData.getArray(formatKey(key), index);
	}

	@Override
	public Object[] getArray(String key) {
		return parentData.getArray(formatKey(key));
	}

	@Override
	public void deleteArray(String key, int index) {
		parentData.deleteArray(formatKey(key), index);
	}

	@Override
	public void removeArray(String key, int index) {
		parentData.removeArray(formatKey(key), index);
	}
}
