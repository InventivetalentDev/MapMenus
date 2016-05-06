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

import com.google.gson.annotations.Expose;
import lombok.*;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmenus.MapMenusPlugin;
import org.inventivetalent.mapmenus.bounds.FixedBounds;
import org.inventivetalent.mapmenus.menu.CursorPosition;
import org.inventivetalent.mapmenus.script.IScriptContainer;
import org.inventivetalent.mapmenus.script.ScriptManagerAbstract;
import org.inventivetalent.scriptconfig.NoSuchFunctionException;
import org.inventivetalent.scriptconfig.RuntimeScriptException;
import org.inventivetalent.scriptconfig.api.ScriptConfig;

import java.awt.*;
import java.util.UUID;
import java.util.logging.Level;

@Data
@EqualsAndHashCode(callSuper = true,
				   doNotUseGetters = true)
@ToString(callSuper = true,
		  doNotUseGetters = true)
@NoArgsConstructor
public class FileScriptComponent extends ScriptComponentAbstract implements IScriptContainer {

	@Expose private String       scriptName;
	private         ScriptConfig script;

	private Object[] initArgs = new Object[0];

	public FileScriptComponent(@NonNull UUID uuid, @NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds, @NonNull String scriptName, Object[] initArgs) {
		super(uuid, parentBounds, bounds);
		this.scriptName = scriptName;
		this.initArgs = initArgs;
	}

	public FileScriptComponent(@NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds, @NonNull String scriptName, Object[] initArgs) {
		this(UUID.randomUUID(), parentBounds, bounds, scriptName, initArgs);
	}

	public ScriptConfig getScript() {
		return script;
	}

	@Override
	public void setScript(String scriptName) {
		this.scriptName = scriptName;
	}

	@Override
	public void reloadScript() {
		reloadScript(MapMenusPlugin.instance.componentScriptManager);
	}

	public void reloadScript(ScriptManagerAbstract scriptManager) {
		if (this.scriptName == null || this.scriptName.isEmpty()) {
			throw new IllegalStateException("Script not yet set");
		}
		if (!scriptManager.doesScriptExist(this.scriptName)) {
			MapMenusPlugin.instance.getLogger().warning("Component Script '" + script + "' does not exist (any longer)");
			return;
		}
		this.script = scriptManager.wrapScript(this.scriptName);

		initScriptVariables();
		try {
			this.script.callFunction("init", this.initArgs);
		} catch (NoSuchFunctionException e) {
			// Ignore
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling init(): " + e.getException().getMessage(), e);
		}
	}

	void initScriptVariables() {
		this.script.setVariable("menu", this.menu);
		this.script.setVariable("component", this);
		this.script.setVariable("data", this.data);
		this.script.setVariable("states", this.states);
		this.script.setVariable("providers", this.providers);
//		this.script.setVariable("placeholders", this.placeholders);
	}

	@Override
	protected void tick0() throws NoSuchFunctionException, RuntimeScriptException {
		script.callFunction("tick");
	}

	@Override
	protected void render0(Graphics2D graphics, Player player) {
		script.callFunction("render", graphics, player);
	}

	@Override
	protected Object click0(Player player, CursorPosition relativePosition, CursorPosition absolutePosition, int action) {
		return script.callFunction("click", player, relativePosition, absolutePosition, action);
	}

	@Override
	protected void dispose0() throws NoSuchFunctionException, RuntimeScriptException {
		script.callFunction("dispose");
	}
}
