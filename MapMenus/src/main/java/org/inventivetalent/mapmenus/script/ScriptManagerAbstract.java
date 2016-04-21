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

package org.inventivetalent.mapmenus.script;

import org.inventivetalent.mapmenus.MapMenusPlugin;
import org.inventivetalent.scriptconfig.RuntimeScriptException;
import org.inventivetalent.scriptconfig.api.ScriptConfig;

import java.io.File;
import java.io.FileNotFoundException;

public abstract class ScriptManagerAbstract {

	protected final MapMenusPlugin plugin;
	protected final File           directory;

	public ScriptManagerAbstract(MapMenusPlugin plugin, File directory) {
		this.plugin = plugin;
		this.directory = directory;
		if (!this.directory.exists()) { this.directory.exists(); }
	}

	public void saveDefaultFiles() {
		for (String s : getDefaultFiles()) {
			plugin.saveResource(new File(this.directory, s).toString().substring("plugins/MapMenus/".length()), false);
		}
	}

	protected abstract String[] getDefaultFiles();

	public boolean doesScriptExist(String name) {
		File file = getScriptFile(name);
		return file != null && file.exists();
	}

	public File getScriptFile(String name) {
		name += ".js";
		name = name.toLowerCase();
		for (String string : directory.list()) {
			if (string.toLowerCase().equals(name)) {
				return new File(directory, string);
			}
		}
		return null;
	}

	public ScriptConfig wrapScript(String name) {
		try {
			return plugin.scriptProvider.load(getScriptFile(name));
		} catch (FileNotFoundException e) {
			throw new RuntimeScriptException("Unexpected Exception", e);
		}
	}

}
