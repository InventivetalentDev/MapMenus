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

package org.inventivetalent.mapmenus.provider;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.mapmenus.MapMenusPlugin;

public abstract class MenuProvider {

	private final String name;

	/**
	 * Initialize a new provider
	 *
	 * @param name Name of the provider
	 * @throws IllegalArgumentException if the name is null or empty
	 */
	public MenuProvider(@NonNull String name) {
		if (name == null || name.isEmpty()) { throw new IllegalArgumentException("provider name cannot be null or empty"); }
		this.name = name;
	}

	/**
	 * Registers this provider
	 *
	 * @return <code>true</code> if the new provider was registered, <code>false</code> if the MapMenus plugin is disabled, or the provider is already registered
	 */
	public final boolean register() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("MapMenus");
		if (plugin == null || !plugin.isEnabled()) {
			return false;
		}
		try {
			((MapMenusPlugin) plugin).menuProviders.registerProvider(this);
			return true;
		} catch (Exception ignored) {
		}
		return false;
	}

	/**
	 * @return The provider's name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return The author of the provider
	 */
	public abstract String getAuthor();

}
