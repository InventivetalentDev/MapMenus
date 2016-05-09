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

package org.inventivetalent.mapmenus.provider.internal;

import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.eventcallbacks.EventCallback;
import org.inventivetalent.eventcallbacks.EventCallbacks;
import org.inventivetalent.mapmenus.provider.MenuProvider;
import org.inventivetalent.reflection.resolver.ClassResolver;

/**
 * Provider to listen for events
 * <p>
 * Bukkit events can either be specified with their full package name, or just as the class name.
 * <p>
 * External events have to be specified as the full package name.
 */
public class EventProvider extends MenuProvider {

	public static final String[]      EVENT_CLASS_FORMATS = {
			"%s",

			"org.bukkit.event.%s",
			"org.bukkit.event.block.%s",
			"org.bukkit.event.enchantment.%s",
			"org.bukkit.event.entity.%s",
			"org.bukkit.event.hanging.%s",
			"org.bukkit.event.inventory.%s",
			"org.bukkit.event.player.%s",
			"org.bukkit.event.server.%s",
			"org.bukkit.event.vehicle.%s",
			"org.bukkit.event.weather.%s",
			"org.bukkit.event.world.%s" };
	static final        ClassResolver CLASS_RESOLVER      = new ClassResolver();

	private final EventCallbacks eventCallbacks;

	public EventProvider(Plugin plugin) {
		super("Events");
		this.eventCallbacks = EventCallbacks.of(plugin);
	}

	@Override
	public String getAuthor() {
		return "inventivetalent";
	}

	/**
	 * Listens for an event
	 *
	 * @param eventClassName Name of the event class
	 * @param priority       listener priority
	 * @param callback       function to call when the event occurs
	 */
	public void listen(String eventClassName, EventPriority priority, EventCallback callback) {
		Class eventClass = findEventClass(eventClassName);
		if (eventClass == null) {
			throw new RuntimeException(new ClassNotFoundException("Event class '" + eventClassName + "' not found"));
		}
		this.eventCallbacks.listenFor(eventClass, priority, callback);
	}

	/**
	 * Listens for an event
	 *
	 * @param eventClassName Name of the event class
	 * @param callback       function to call when the event occurs
	 */
	public void listen(String eventClassName, EventCallback callback) {
		listen(eventClassName, EventPriority.NORMAL, callback);
	}

	/**
	 * Check if an event class exists
	 *
	 * @param eventClassName Name of the event class
	 * @return <code>true</code> if the event exists
	 */
	public boolean doesEventExist(String eventClassName) {
		return findEventClass(eventClassName) != null;
	}

	/**
	 * Check if an event class exists
	 *
	 * @param eventClassName Name of the event class
	 * @return <code>true</code> if the event exists
	 * @see #doesEventExist(String)
	 */
	public boolean exists(String eventClassName) {
		return doesEventExist(eventClassName);
	}

	/**
	 * Attempts to find a event class
	 *
	 * @param className name of the class
	 * @return the class, or <code>null</code>
	 */
	public Class<?> findEventClass(String className) {
		String[] queries = new String[EVENT_CLASS_FORMATS.length];
		for (int i = 0; i < queries.length; i++)
			queries[i] = String.format(EVENT_CLASS_FORMATS[i], className);

		return CLASS_RESOLVER.resolveSilent(queries);
	}

}
