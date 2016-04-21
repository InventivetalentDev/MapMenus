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

package org.inventivetalent.mapmenus;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.inventivetalent.boundingbox.BoundingBox;
import org.inventivetalent.vectors.Vectors;
import org.inventivetalent.vectors.d2.Vector2DDouble;
import org.inventivetalent.vectors.d3.Vector3DDouble;

public enum MapFacing {

	// @formatter:off
	//		faceX	faceZ	frameX	frameZ									inverted
	NORTH(	0, 		-1, 	-1, 	0, 		BlockFace.NORTH, 	Plane.X,	true	),
	EAST(	+1, 	0, 		0, 		-1, 	BlockFace.EAST, 	Plane.Z,	true	),
	SOUTH(	0, 		+1, 	+1,		0, 		BlockFace.SOUTH, 	Plane.X,	false	),
	WEST(	-1, 	0, 		0, 		+1, 	BlockFace.WEST, 	Plane.Z,	false	);
	// @formatter:on

	// Mod in the direction the frame is facing
	private int xFaceMod;
	private int zFaceMod;

	// Mod on the frame pane
	private int xFrameMod;
	private int zFrameMod;

	// Facing of the frame
	private BlockFace frameDirection;

	private Plane plane;

	private boolean frameModInverted;

	MapFacing(int xFaceMod, int zFaceMod, int xFrameMod, int zFrameMod, BlockFace frameDirection, Plane plane, boolean frameModInverted) {
		this.xFaceMod = xFaceMod;
		this.zFaceMod = zFaceMod;

		this.xFrameMod = xFrameMod;
		this.zFrameMod = zFrameMod;

		this.frameDirection = frameDirection;
		this.plane = plane;
		this.frameModInverted = frameModInverted;
	}

	public int getFaceModX() {
		return xFaceMod;
	}

	public int getFaceModZ() {
		return zFaceMod;
	}

	public int getFrameModX() {
		return xFrameMod;
	}

	public int getFrameModZ() {
		return zFrameMod;
	}

	public BlockFace getFrameDirection() {
		return frameDirection;
	}

	public Plane getPlane() {
		return plane;
	}

	//	public static MapFacing getForBoundingBox(BoundingBox boundingBox) {
	//		Vector a = new Vector(boundingBox.minX, 0, boundingBox.minZ);
	//		Vector b = new Vector(boundingBox.maxX, 0, boundingBox.maxZ);
	//
	//	}

	public boolean isFrameModInverted() {
		return frameModInverted;
	}

	public static MapFacing getForItemFrame(ItemFrame itemFrame) {
		switch (itemFrame.getFacing()) {
			case NORTH:
				return NORTH;
			case EAST:
				return EAST;
			case SOUTH:
				return SOUTH;
			case WEST:
				return WEST;
			default:
				throw new RuntimeException("Invalid frame facing: " + itemFrame.getFacing());
		}
	}

	public BoundingBox createBoundingBox(Vector3DDouble a, Vector3DDouble b) {
		Vector3DDouble min = Vectors.min(a, b);
		Vector3DDouble max = Vectors.max(a, b);

		Plane plane = getPlane();
		if (plane == Plane.X) {
			if (getFaceModZ() > 0) {
				max = max.add(1, 1, 0.0625);
			}
			if (getFaceModZ() < 0) {
				min = min.add(0, 0, 0.9375);
				max = max.add(1, 1,  0.9375);
			}
		}
		if (plane == Plane.Z) {
			if (getFaceModX() > 0) {
				max = max.add(0.0625, 1, 1);
			}
			if (getFaceModX() < 0) {
				min = min.add(0.9375, 0, 0);
				max = max.add(0.9375, 1, 1);
			}
		}

		return new BoundingBox(min, max);
	}

	public enum Plane {
		/**
		 * X-Plane, maps facing south or north
		 */
		X,
		/**
		 * Z-Plane, maps facing east or west
		 */
		Z;

		public Vector2DDouble to2D(Vector3DDouble vector3d) {
			if (this == X) {
				return new Vector2DDouble(vector3d.getX(), vector3d.getY());
			}
			if (this == Z) {
				return new Vector2DDouble(vector3d.getZ(), vector3d.getY());
			}

			// No idea how it would ever get this far...
			return Vector2DDouble.ZERO;
		}

		public Vector3DDouble to3D(Vector2DDouble vector2d) {
			return to3D(vector2d, 0);
		}

		public Vector3DDouble to3D(Vector2DDouble vector2d, double xOrZ) {
			return to3D(vector2d, xOrZ, xOrZ);
		}

		public Vector3DDouble to3D(Vector2DDouble vector2d, double altX, double altZ) {
			if (this == X) {
				return new Vector3DDouble(vector2d.getX(), vector2d.getY(), altZ);
			}
			if (this == Z) {
				return new Vector3DDouble(altX, vector2d.getY(), vector2d.getX());
			}

			return Vector3DDouble.ZERO;
		}

	}

}
