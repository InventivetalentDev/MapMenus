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

import lombok.*;
import org.bukkit.entity.Player;
import org.inventivetalent.mapmenus.MapMenusPlugin;
import org.inventivetalent.mapmenus.MenuScriptException;
import org.inventivetalent.mapmenus.bounds.FixedBounds;
import org.inventivetalent.mapmenus.menu.CursorPosition;
import org.inventivetalent.mapmenus.menu.ScriptMapMenu;
import org.inventivetalent.mapmenus.menu.data.IData;
import org.inventivetalent.mapmenus.menu.data.IStates;
import org.inventivetalent.mapmenus.provider.MenuProviders;
import org.inventivetalent.mapmenus.provider.internal.PlaceholderProvider;
import org.inventivetalent.mapmenus.script.Scriptify;
import org.inventivetalent.scriptconfig.NoSuchFunctionException;
import org.inventivetalent.scriptconfig.RuntimeScriptException;

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
@AllArgsConstructor
public abstract class ScriptComponentAbstract extends MenuComponentAbstract {

	public int id;

	private boolean noTickFunction;
	private boolean noRenderFunction;
	private boolean noClickFunction;

	private boolean visible = true;

	// Script references
	@Scriptify public ScriptMapMenu menu;
	@Scriptify public IData         data;
	@Scriptify public IStates       states;
	@Scriptify public ScriptComponentAbstract component    = this;
	@Scriptify public MenuProviders           providers    = MapMenusPlugin.instance.menuProviders;
	@Scriptify public PlaceholderProvider     placeholders = providers.get("Placeholders");

	public ScriptComponentAbstract(@NonNull UUID uuid, @NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds) {
		super(uuid, parentBounds, bounds);
	}

	@Scriptify(targetVar = "component")
	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Scriptify(targetVar = "component")
	@Override
	public boolean isVisible() {
		return visible;
	}

	@Scriptify(targetVar = "component")
	@Override
	public void show() {
		setVisible(true);
	}

	@Scriptify(targetVar = "component")
	@Override
	public void hide() {
		setVisible(false);
	}

	@Override
	public void tick() {
		if (noTickFunction) { return; }
		try {
			tick0();
		} catch (NoSuchFunctionException e) {
			// Ignore this
			if (MapMenusPlugin.instance.debug) {
				MapMenusPlugin.instance.getLogger().log(Level.INFO, "[Ignored]", e);
			}
			noTickFunction = true;
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling tick(): " + e.getException().getMessage(), e);
			throw new MenuScriptException("ScriptException in Component tick()", e);
		}
	}

	protected abstract void tick0() throws NoSuchFunctionException, RuntimeScriptException;

	@Override
	public void render(Graphics2D graphics, Player player) {
		if (noRenderFunction) { return; }
		if (!visible) { return; }
		try {
			// Translate to the component's position
//			graphics.translate(getAbsoluteBounds().x, getAbsoluteBounds().y);

			render0(graphics, player);

			// Translate back
//			graphics.translate(-getAbsoluteBounds().x, -getAbsoluteBounds().y);
		} catch (NoSuchFunctionException e) {
			// Ignore this, the element doesn't want to be rendered
			noRenderFunction = true;
			if (MapMenusPlugin.instance.debug) {
				MapMenusPlugin.instance.getLogger().log(Level.INFO, "[Ignored]", e);
			}
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling render(): " + e.getException().getMessage(), e);
			throw new MenuScriptException("ScriptException in Component render()", e);
		}
	}

	protected abstract void render0(Graphics2D graphics2D, Player player) throws NoSuchFunctionException, RuntimeScriptException;

	@Override
	public boolean click(Player player, CursorPosition absolutePosition, int action) {
		if (noClickFunction) { return false; }
		if (!visible) { return false; }
		if (!getBounds().contains(absolutePosition.getX(), absolutePosition.getY())) {
			return false;
		}

		try {
			CursorPosition relativePosition = new CursorPosition(absolutePosition.getX() - getBounds().getX(), absolutePosition.getY() - getBounds().getY());
			Object result = click0(player, relativePosition, absolutePosition, action);
			return result != null;
		} catch (NoSuchFunctionException e) {
			// Ignore this
			noClickFunction = true;
			if (MapMenusPlugin.instance.debug) {
				MapMenusPlugin.instance.getLogger().log(Level.INFO, "[Ignored]", e);
			}
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling click(): " + e.getException().getMessage(), e);
			throw new MenuScriptException("ScriptException in Component click()", e);
		}
		return false;
	}

	protected abstract Object click0(Player player, CursorPosition relativePosition, CursorPosition absolutePosition, int action) throws NoSuchFunctionException, RuntimeScriptException;

	@Override
	public void dispose() {
		try {
			dispose0();
		} catch (NoSuchFunctionException e) {
			// Ignore this
			if (MapMenusPlugin.instance.debug) {
				MapMenusPlugin.instance.getLogger().log(Level.INFO, "[Ignored]", e);
			}
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling dispose(): " + e.getException().getMessage(), e);
		}

		if (this.menu != null) {
			this.menu.removeComponent(getUuid());
		}
	}

	protected abstract void dispose0() throws NoSuchFunctionException, RuntimeScriptException;
}
