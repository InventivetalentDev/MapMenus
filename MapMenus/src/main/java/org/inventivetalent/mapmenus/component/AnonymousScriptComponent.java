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

package org.inventivetalent.mapmenus.component;

import jdk.nashorn.api.scripting.JSObject;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmenus.bounds.FixedBounds;
import org.inventivetalent.mapmenus.menu.CursorPosition;
import org.inventivetalent.scriptconfig.NoSuchFunctionException;
import org.inventivetalent.scriptconfig.RuntimeScriptException;

import java.awt.*;
import java.util.UUID;

public class AnonymousScriptComponent extends ScriptComponentAbstract {

	private JSObject script;

	public AnonymousScriptComponent(@NonNull UUID uuid, @NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds, @NonNull JSObject script) {
		super(uuid, parentBounds, bounds);
		this.script = script;
	}

	public AnonymousScriptComponent(@NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds, @NonNull JSObject script) {
		this(UUID.randomUUID(), parentBounds, bounds, script);
	}

	@Override
	protected void tick0() throws NoSuchFunctionException, RuntimeScriptException {
		((JSObject) script.getMember("tick")).call(this);
	}

	@Override
	protected void render0(Graphics2D graphics, Player player) throws NoSuchFunctionException, RuntimeScriptException {
		((JSObject) script.getMember("render")).call(this, graphics, player);
	}

	@Override
	protected Object click0(Player player, CursorPosition relativePosition, CursorPosition absolutePosition, int action) throws NoSuchFunctionException, RuntimeScriptException {
		return ((JSObject) script.getMember("click")).call(this, player, relativePosition, absolutePosition, action);
	}

	@Override
	protected void dispose0() throws NoSuchFunctionException, RuntimeScriptException {
		((JSObject) script.getMember("dispose")).call(this);
	}
}
