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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmenus.MapMenusPlugin;
import org.inventivetalent.mapmenus.MenuScriptExecutionException;
import org.inventivetalent.mapmenus.bounds.FixedBounds;
import org.inventivetalent.mapmenus.menu.CursorPosition;
import org.inventivetalent.mapmenus.menu.ScriptMapMenu;
import org.inventivetalent.mapmenus.menu.data.IData;
import org.inventivetalent.mapmenus.menu.data.IStates;
import org.inventivetalent.mapmenus.script.IScriptContainer;
import org.inventivetalent.mapmenus.script.ScriptManagerAbstract;
import org.inventivetalent.scriptconfig.NoSuchFunctionException;
import org.inventivetalent.scriptconfig.RuntimeScriptException;
import org.inventivetalent.scriptconfig.api.ScriptConfig;

import java.awt.*;
import java.util.UUID;
import java.util.logging.Level;

@EqualsAndHashCode(callSuper = true,
				   doNotUseGetters = true,
				   exclude = {
						   "component",
						   "menu" })
@ToString(callSuper = true,
		  doNotUseGetters = true,
		  exclude = {
				  "component",
				  "menu" })
@NoArgsConstructor
public class ScriptComponent extends MenuComponentAbstract implements IScriptContainer {

	public          int          id;
	@Expose private String       scriptName;
	private         ScriptConfig script;

	private Object[] initArgs = new Object[0];

	private boolean noTickFunction;
	private boolean noRenderFunction;
	private boolean noClickFunction;

	// Script references
	public ScriptMapMenu menu;
	public IData         data;
	public IStates       states;
	public ScriptComponent component = this;

	public ScriptComponent(@NonNull UUID uuid, @NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds, @NonNull String scriptName, Object[] initArgs) {
		super(uuid, parentBounds, bounds);
		this.scriptName = scriptName;
		this.initArgs = initArgs;
	}

	public ScriptComponent(@NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds, @NonNull String scriptName, Object[] initArgs) {
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
	}

	@Override
	public void tick() {
		if (noTickFunction) { return; }
		try {
			script.callFunction("tick");
		} catch (NoSuchFunctionException e) {
			// Ignore this
			noTickFunction = true;
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling tick(): " + e.getException().getMessage(), e);
			throw new MenuScriptExecutionException("ScriptException in Component tick()", e);
		}
	}

	@Override
	public void render(Graphics2D graphics, Player player) {
		if (noRenderFunction) { return; }
		try {
			script.callFunction("render", graphics, player);
		} catch (NoSuchFunctionException e) {
			// Ignore this, the element doesn't want to be rendered
			noRenderFunction = true;
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling render(): " + e.getException().getMessage(), e);
			throw new MenuScriptExecutionException("ScriptException in Component render()", e);
		}
	}

	@Override
	public boolean click(Player player, CursorPosition absolutePosition, int action) {
		if (noClickFunction) { return false; }
		if (!getBounds().contains(absolutePosition.getX(), absolutePosition.getY())) {
			return false;
		}

		try {
			CursorPosition relativePosition = new CursorPosition(absolutePosition.getX() - getBounds().getX(), absolutePosition.getY() - getBounds().getY());
			Object result = script.callFunction("click", player, relativePosition, absolutePosition, action);
			return result != null;
		} catch (NoSuchFunctionException e) {
			// Ignore this
			noClickFunction = true;
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling click(): " + e.getException().getMessage(), e);
			throw new MenuScriptExecutionException("ScriptException in Component click()", e);
		}
		return false;
	}

	@Override
	public void dispose() {
		try {
			script.callFunction("dispose");
		} catch (NoSuchFunctionException e) {
			// Ignore this
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling dispose(): " + e.getException().getMessage(), e);
		}

		if (this.menu != null) {
			this.menu.removeComponent(getUuid());
		}
	}
}
