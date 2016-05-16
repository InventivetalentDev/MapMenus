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
import jdk.nashorn.api.scripting.JSObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.inventivetalent.mapmenus.MapMenusPlugin;
import org.inventivetalent.mapmenus.bounds.FixedBounds;
import org.inventivetalent.mapmenus.script.IScriptContainer;
import org.inventivetalent.mapmenus.script.ScriptManagerAbstract;
import org.inventivetalent.mapmenus.script.Scriptifier;
import org.inventivetalent.scriptconfig.api.ScriptConfig;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true,
				   doNotUseGetters = true)
@ToString(callSuper = true,
		  doNotUseGetters = true)
public class FileScriptComponent extends AnonymousScriptComponent implements IScriptContainer {

	@Expose private String       scriptName;
	private         ScriptConfig scriptConfig;

	private Object[] initArgs = new Object[0];

	public FileScriptComponent(@NonNull UUID uuid, @NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds, @NonNull String scriptName, Object[] initArgs) {
		super(uuid, parentBounds, bounds);
		this.scriptName = scriptName;
		this.initArgs = initArgs;
	}

	public FileScriptComponent(@NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds, @NonNull String scriptName, Object[] initArgs) {
		this(UUID.randomUUID(), parentBounds, bounds, scriptName, initArgs);
	}

	//	public ScriptConfig getScript() {
	//		return scriptConfig;
	//	}

	public void setScriptConfig(String scriptName) {
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
			MapMenusPlugin.instance.getLogger().warning("Component Script '" + scriptConfig + "' does not exist (any longer)");
			return;
		}
		this.scriptConfig = scriptManager.wrapScript(this.scriptName);
		this.script = (JSObject) this.scriptConfig.getContent();

		initScriptVariables();
		super.init(this.initArgs);
	}

	void initScriptVariables() {
		Scriptifier.scriptify(this, getScript());
	}

}
