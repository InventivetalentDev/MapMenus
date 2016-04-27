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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.inventivetalent.boundingbox.BoundingBox;
import org.inventivetalent.boundingbox.BoundingBoxAPI;
import org.inventivetalent.mapmenus.IClickable;
import org.inventivetalent.mapmenus.IDisposable;
import org.inventivetalent.mapmenus.ITickable;
import org.inventivetalent.mapmenus.MapFacing;
import org.inventivetalent.mapmenus.bounds.FixedBounds;
import org.inventivetalent.mapmenus.component.MenuComponentAbstract;
import org.inventivetalent.mapmenus.component.ScriptComponent;
import org.inventivetalent.mapmenus.render.IRenderable;
import org.inventivetalent.vectors.d2.Vector2DDouble;
import org.inventivetalent.vectors.d3.Vector3DDouble;
import org.inventivetalent.vectors.d3.Vector3DInt;

import java.util.*;

@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
@NoArgsConstructor
public abstract class MapMenuAbstract implements IRenderable, ITickable, IClickable, IDisposable {

	@Expose protected String         worldName;
	@Expose protected Vector3DDouble blockBaseVector;
	@Expose protected Vector3DDouble baseVector;
	@Expose protected BoundingBox    boundingBox;
	@Expose protected BoundingBox    blockBoundingBox;
	@Expose protected FixedBounds    bounds;
	@Expose protected MapFacing      facing;

	@Expose protected Vector2DDouble minCorner2d;
	@Expose protected Vector2DDouble maxCorner2d;

	// Don't expose the components; the script is responsible for adding them
	/*@Expose*/ protected final Map<UUID, ScriptComponent> components = new HashMap<>();

	protected boolean tickLocked;

	public MapMenuAbstract(@NonNull ItemFrame baseFrame, @NonNull Vector3DDouble firstCorner, @NonNull Vector3DDouble secondCorner) {
		Vector3DDouble diffCheck = firstCorner.subtract(secondCorner);
		if (Math.abs(diffCheck.getX()) > 1 && Math.abs(diffCheck.getZ()) > 1) {
			// The frames are 3-dimensional, not on a single plane
			throw new IllegalArgumentException("Invalid frame position dimensions");
		}

		this.worldName = baseFrame.getWorld().getName();
		this.facing = MapFacing.getForItemFrame(baseFrame);
		this.blockBaseVector = new Vector3DDouble(baseFrame.getLocation().getBlock().getRelative(this.facing.getFrameDirection().getOppositeFace()).getLocation());
		this.baseVector = new Vector3DDouble(baseFrame.getLocation().getBlockX(), baseFrame.getLocation().getBlockY(), baseFrame.getLocation().getBlockZ());

		Vector3DDouble firstVector = new Vector3DDouble(new Vector3DInt(firstCorner));
		Vector3DDouble secondVector = new Vector3DDouble(new Vector3DInt(secondCorner));
		this.boundingBox = this.facing.createBoundingBox(firstVector, secondVector);
		// Combine with the base-block vector to expand it into the block

		this.blockBoundingBox = this.boundingBox.combine(BoundingBoxAPI.getAbsoluteBoundingBox(this.blockBaseVector.toBukkitLocation(getWorld()).getBlock()));

		this.minCorner2d = this.facing.getPlane().to2D(boundingBox.getMinVector());
		this.maxCorner2d = this.facing.getPlane().to2D(boundingBox.getMaxVector());

		this.bounds = new FixedBounds(0, 0, getBlockWidth() * 128, getBlockHeight() * 128);

		System.out.println("BoundingBox: " + boundingBox);
		System.out.println("Facing: " + facing);
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

	public World getWorld() {
		return Bukkit.getWorld(worldName);
	}

	public int getBlockWidth() {
		//		if (this.facing.getFrameModX() != 0) {
		//			return (int) Math.ceil(Math.abs(this.facing.getFrameModX() * (this.boundingBox.maxX - this.boundingBox.minX)));
		//		}
		//		if (this.facing.getFrameModZ() != 0) {
		//			return (int) Math.ceil(Math.abs(this.facing.getFrameModZ() * (this.boundingBox.maxZ - this.boundingBox.minZ)));
		//		}
		//		// This should (in theory) never happen
		//		MapMenusPlugin.instance.getLogger().warning("getBlockWidth is 0!");
		//		return 0;

		return this.maxCorner2d.getX().intValue() - this.minCorner2d.getX().intValue();
	}

	public int getBlockHeight() {
		//		return ((int) (this.boundingBox.maxY - this.boundingBox.minY));

		return this.maxCorner2d.getY().intValue() - this.minCorner2d.getY().intValue();
	}

	public FixedBounds getBounds() {
		return bounds;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public Set<UUID> getComponentIds() {
		return new HashSet<>(components.keySet());
	}

	public Set<ScriptComponent> getComponents() {
		return new HashSet<>(components.values());
	}

	public boolean isOnBlock(Vector3DDouble blockVector) {
		blockVector = blockVector.add(new Vector3DDouble(.5, .5, .5));

		return blockBoundingBox.contains(blockVector);
	}
}

