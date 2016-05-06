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

package org.inventivetalent.mapmenus.menu;

import com.google.gson.annotations.Expose;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bukkit.entity.ItemFrame;
import org.inventivetalent.frameutil.BaseFrameMapAbstract;
import org.inventivetalent.mapmenus.IClickable;
import org.inventivetalent.mapmenus.IDisposable;
import org.inventivetalent.mapmenus.ITickable;
import org.inventivetalent.mapmenus.bounds.FixedBounds;
import org.inventivetalent.mapmenus.component.MenuComponentAbstract;
import org.inventivetalent.mapmenus.component.ScriptComponentAbstract;
import org.inventivetalent.mapmenus.render.IRenderable;
import org.inventivetalent.vectors.d3.Vector3DDouble;

import java.util.*;

@EqualsAndHashCode(doNotUseGetters = true,
				   callSuper = true)
@ToString(doNotUseGetters = true)
@NoArgsConstructor
public abstract class MapMenuAbstract extends BaseFrameMapAbstract implements IRenderable, ITickable, IClickable, IDisposable {

	@Expose protected FixedBounds bounds;

	// Don't expose the components; the script is responsible for adding them
	/*@Expose*/ protected final Map<UUID, ScriptComponentAbstract> components = new HashMap<>();

	protected boolean tickLocked;

	public MapMenuAbstract(@NonNull ItemFrame baseFrame, @NonNull Vector3DDouble firstCorner, @NonNull Vector3DDouble secondCorner) {
		super(baseFrame, firstCorner, secondCorner);

		this.bounds = new FixedBounds(0, 0, getBlockWidth() * 128, getBlockHeight() * 128);
	}

	public MenuComponentAbstract removeComponent(UUID uuid) {
		tickLocked = true;
		MenuComponentAbstract removed = components.remove(uuid);
		tickLocked = false;
		return removed;
	}

	public MenuComponentAbstract getComponent(UUID uuid) {
		tickLocked = true;
		MenuComponentAbstract component = components.get(uuid);
		tickLocked = false;
		return component;
	}

	public FixedBounds getBounds() {
		return bounds;
	}

	public Set<UUID> getComponentIds() {
		return new HashSet<>(components.keySet());
	}

	public Set<ScriptComponentAbstract> getComponents() {
		return new HashSet<>(components.values());
	}


}

