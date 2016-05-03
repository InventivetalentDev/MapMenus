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

import lombok.Synchronized;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class PlaceholderProvider {

	private MapMenusPlugin plugin;
	private final Set<IPlaceholderReplacer> replacers = new HashSet<>();

	public PlaceholderProvider(MapMenusPlugin plugin) {
		this.plugin = plugin;
	}

	@Synchronized
	public void addReplacer(IPlaceholderReplacer replacer) {
		replacers.add(replacer);
	}

	@Synchronized
	public String replace(Player player, String string) {
		System.out.println("replace "+string);
		for (IPlaceholderReplacer replacer : this.replacers)
			string = replacer.replace(player, string);
		return string;
	}

	@Synchronized
	public String apply(Player player, String string) {
		return replace(player, string);
	}

	public interface IPlaceholderReplacer {
		String replace(Player player, String string);
	}

}
