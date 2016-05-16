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
import lombok.Synchronized;
import org.inventivetalent.mapmenus.MapMenusPlugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MenuProviders {

	private MapMenusPlugin plugin;
	private Map<String, MenuProvider> providerMap = new HashMap<>();

	public MenuProviders(MapMenusPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Registers a provider
	 *
	 * @param provider MenuProvider to register
	 * @throws IllegalArgumentException if the provider is already registered
	 * @see MenuProvider#register()
	 */
	@Synchronized
	public void registerProvider(@NonNull MenuProvider provider) {
		if (providerMap.containsKey(provider.getName())) { throw new IllegalArgumentException("Provider '" + provider.getName() + "' is already registered"); }
		providerMap.put(provider.getName(), provider);
	}

	/**
	 * Check if a provider is available
	 *
	 * @param name name of the provider
	 * @return <code>true</code> if the provider is available, <code>false</code> otherwise
	 */
	@Synchronized
	public boolean isAvailable(@Nullable String name) {
		if (name == null) { return false; }
		return providerMap.containsKey(name);
	}

	/**
	 * Check if a provider is available
	 *
	 * @param name name of the provider
	 * @return <code>true</code> if the provider is available, <code>false</code> otherwise
	 * @see #isAvailable(String)
	 */
	@Synchronized
	public boolean has(@Nullable String name) {
		if (name == null) { return false; }
		return providerMap.containsKey(name);
	}

	/**
	 * Get a provider
	 *
	 * @param name name of the provider
	 * @param <T>  (optional) type of the provider class
	 * @return the provider if it is registered, <code>null</code> otherwise
	 * @see #isAvailable(String)
	 */
	@Synchronized
	public <T extends MenuProvider> T get(@NonNull String name) {
		return (T) providerMap.get(name);
	}

}
