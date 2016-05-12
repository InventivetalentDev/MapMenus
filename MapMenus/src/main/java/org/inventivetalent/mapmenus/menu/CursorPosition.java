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
import lombok.Data;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.inventivetalent.boundingbox.BoundingBox;
import org.inventivetalent.boundingbox.BoundingBoxAPI;
import org.inventivetalent.mapmanager.TimingsHelper;
import org.inventivetalent.vectors.d2.Vector2DDouble;
import org.inventivetalent.vectors.d3.Vector3DDouble;

import java.util.*;

@Data
@ToString(callSuper = true,
		  doNotUseGetters = true)
public class CursorPosition {

	@Expose public final int x, y;

	public CursorPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public static CursorResult calculateRaw(Player player, int cursorDistance) {
		TimingsHelper.startTiming("MapMenu:CursorPosition:calculate:raw");

		CursorResult result = new CursorResult();

		final Location location = player.getLocation();
		final Vector3DDouble locationVector = new Vector3DDouble(location.toVector());
		final Vector3DDouble direction = new Vector3DDouble(location.getDirection());

		// Get the player's target block to find nearby entities
		Block targetBlock = player.getTargetBlock((Set<Material>) null, cursorDistance);
		if (targetBlock == null || targetBlock.getType() == Material.AIR) { return null; }

		// Get all entities close to the block
		List<Entity> entities = new ArrayList<>(targetBlock.getWorld().getNearbyEntities(targetBlock.getLocation().add(.5, .5, .5), 1, .5, 1));
		List<BoundingBox> boundingBoxes = new ArrayList<>();
		for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext(); ) {
			Entity entity = iterator.next();
			if (entity.getType() != EntityType.ITEM_FRAME) {
				iterator.remove(); // Filter non-ItemFrame entities
				continue;
			}

			// Get the ItemFrame boundingBox
			BoundingBox boundingBox = BoundingBoxAPI.getAbsoluteBoundingBox(entity);
			double partialX = boundingBox.minX - Math.floor(boundingBox.minX);
			boundingBox = boundingBox.expand(partialX == 0.125 ? 0.125 : 0, 0.125, partialX == 0.125 ? 0 : 0.125);// Expand the bounding box to fit the whole block (not just the center)
			boundingBoxes.add(boundingBox);
		}
		if (entities.isEmpty()) {
			TimingsHelper.stopTiming("MapMenu:CursorPosition:calculate:raw");
			return null; //There are no item-frames on the target block
		}

		Vector3DDouble vectorOnBlock;
		Vector3DDouble lastVector = null;
		double start = Math.max(0, targetBlock.getLocation().distance(player.getLocation()) - 2);
		doubleLoop:
		for (double d = start; d < cursorDistance; d += /*ACCURACY*/0.0125) {
			vectorOnBlock = direction.clone().multiply(d).add(0, player.getEyeHeight(), 0).add(locationVector);
			if (vectorOnBlock.toBukkitLocation(location.getWorld()).getBlock().getType() != Material.AIR) {// Block is solid -> we've hit the target block
				result.blockHit = true;
				break;
			}
			for (BoundingBox boundingBox : boundingBoxes) {
				if (boundingBox.contains(vectorOnBlock)) {
					result.entityHit = true;
					break doubleLoop;// We've hit one of the item frames (hopefully the target one)
				}
			}
			lastVector = vectorOnBlock;
		}
		if (lastVector != null) {
			// Find the frame closest to the target vector
			ItemFrame closestFrame = null;
			double closest = 2.0;
			for (Entity entity : entities) {
				double distance = lastVector.distance(new Vector3DDouble(entity.getLocation().toVector()));
				if (distance < closest) {
					closest = distance;
					closestFrame = (ItemFrame) entity;
				}
			}
			if (closestFrame == null) {
				TimingsHelper.stopTiming("MapMenu:CursorPosition:calculate:raw");
				return null;// This should never happen, since we filtered all the unwanted entities above
			}

			result.vector = lastVector;
			result.found = true;
		}
		TimingsHelper.stopTiming("MapMenu:CursorPosition:calculate:raw");
		return result;
	}

	public static CursorPosition convertVectorToCursor(Vector3DDouble targetVector, ScriptMapMenu mapMenu) {
		TimingsHelper.startTiming("MapMenu:CursorPosition:calculate:convert");
		if (targetVector == null || mapMenu == null) { return null; }

		double menuWidth = mapMenu.getBlockWidth() * 128.0D;
		double menuHeight = mapMenu.getBlockHeight() * 128.0D;

		Vector3DDouble menuVector = targetVector.clone().subtract(new Vector3DDouble(mapMenu.getBoundingBox().minX, mapMenu.getBoundingBox().minY, mapMenu.getBoundingBox().minZ));
		double vecX = menuVector.getX();
		double vecY = menuVector.getY();
		double vecZ = menuVector.getZ();

		vecX = vecX * menuWidth / mapMenu.getBlockWidth();
		vecZ = vecZ * menuWidth / mapMenu.getBlockWidth();
		vecY = vecY * menuHeight / mapMenu.getBlockHeight();

		vecY = menuHeight - vecY;// Flip Y around to match the image coordinates
		if (mapMenu.getFacing().isFrameModInverted()) {
			// Also flip X&Z if the direction is inverted
			vecX = menuWidth - vecX;
			vecZ = menuWidth - vecZ;
		}

		menuVector = new Vector3DDouble(vecX, vecY, vecZ);
		//		int menuX = (int) Math.round(mapMenu.facing.getFrameModX() != 0 ? menuVector.getX() : menuVector.getZ());
		//		int menuY = (int) Math.round(menuVector.getY());
		//
		//		System.out.println("TargetVector: " + targetVector);
		//		System.out.println(" MenuVector: " + menuVector);
		//		System.out.println("   ");
		Vector2DDouble vector2d = mapMenu.getFacing().getPlane().to2D(menuVector);

		TimingsHelper.stopTiming("MapMenu:CursorPosition:calculate:convert");
		return new CursorPosition(vector2d.getX().intValue(), vector2d.getY().intValue());
	}

	public static CursorPosition calculateFor(Player player, ScriptMapMenu mapMenu) {
		TimingsHelper.startTiming("MapMenu:CursorPosition:calculate");

		CursorResult result = calculateRaw(player, mapMenu.options.cursorDistance);
		if (result == null || !result.found) {
			TimingsHelper.stopTiming("MapMenu:CursorPosition:calculate");
			return null;
		}

		//			boolean menuContainsVector = mapMenu.boundingBox.contains(lastVector);
		//			if (!menuContainsVector) { -> disabled, when we hit an entity the vector is not inside of the boundingBox
		//				return null;
		//			}
		boolean contains = mapMenu.getBoundingBox().expand(0.0625).contains(result.vector);
		if (!contains) {
			TimingsHelper.stopTiming("MapMenu:CursorPosition:calculate");
			return null;
		}

		CursorPosition position = convertVectorToCursor(result.vector, mapMenu);

		TimingsHelper.stopTiming("MapMenu:CursorPosition:calculate");
		return position;
	}

	public static CursorMenuQueryResult findMenuByCursor(Player player, Collection<ScriptMapMenu> menus) {
		CursorMenuQueryResult result = new CursorMenuQueryResult();
		for (ScriptMapMenu mapMenu : menus) {
			CursorPosition position = calculateFor(player, mapMenu);
			if (position != null) {
				result.found = true;
				result.position = position;
				result.menu = mapMenu;
				return result;
			}
		}
		return result;
	}

	@Data
	public static class CursorResult {
		boolean        found;
		Vector3DDouble vector;
		boolean        blockHit;
		boolean        entityHit;
	}

	@Data
	public static class CursorMenuQueryResult {
		boolean        found;
		CursorPosition position;
		ScriptMapMenu  menu;
	}

}
