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

import org.bukkit.Location;

public enum MoveDirection {

	// @formatter:off
	//			name		^	v	<	>
	NORTH(		"north", 	2, 	0, 	1,	3),
	EAST(		"east", 	3, 	1, 	2, 	0),
	SOUTH(		"south", 	0, 	2, 	3, 	1),
	WEST(		"west", 	1, 	3, 	0, 	2),
	UP(			"up"),
	DOWN(		"down"),
	NONE(		"?"),

	FORWARD(	"forward"),
	BACKWARD(	"backward"),
	LEFT(		"left"),
	RIGHT(		"right"),

	YAW(		"yaw"),
	PITCH(		"pitch");
	// @formatter:on

	private String codeName;
	private int    dirForward, dirBackward, dirLeft, dirRight;

	MoveDirection(String codeName, int dirForward, int dirBackward, int dirLeft, int dirRight) {
		this.codeName = codeName;
		this.dirForward = dirForward;
		this.dirBackward = dirBackward;
		this.dirLeft = dirLeft;
		this.dirRight = dirRight;
	}

	MoveDirection(String codeName) {
		this.codeName = codeName;
	}

	public String getCodeName() {
		return codeName;
	}

	public MoveDirection getLookDirection(Location location) {
		return getLookDirection(location.getYaw());
	}

	public MoveDirection getLookDirection(float yaw) {
		int cardinal = floor_double(yaw * 4.0F / 360.0F + 0.5D) & 3;
		System.out.println(cardinal);

		if (this != YAW && this != PITCH && this != UP && this != DOWN) {
			if (cardinal == dirForward) { return FORWARD; }
			if (cardinal == dirBackward) { return BACKWARD; }

			// These are inverted for some reason, so just switch them around
			if (cardinal == dirLeft) { return RIGHT; }
			if (cardinal == dirRight) { return LEFT; }
		}

		return this;
	}

	public double getValue(Location relativeMove) {
		switch (this) {
			case EAST:
			case WEST:
				return relativeMove.getX();
			case NORTH:
			case SOUTH:
				return relativeMove.getZ();
			case UP:
			case DOWN:
				return relativeMove.getY();

			case YAW:
				return relativeMove.getYaw();
			case PITCH:
				return relativeMove.getPitch();

			case FORWARD:
			case BACKWARD:
			case LEFT:
			case RIGHT:
			case NONE:
			default:
				return 0;
		}
	}

	public static MoveDirection getBaseDirection(Location relativeMove) {
		double highest = 0;
		MoveDirection direction = NONE;

		if (relativeMove.getX() > highest) {
			highest = relativeMove.getX();
			direction = EAST;
		}
		if (-relativeMove.getX() > highest) {
			highest = -relativeMove.getX();
			direction = WEST;
		}

		if (relativeMove.getZ() > highest) {
			highest = relativeMove.getZ();
			direction = SOUTH;
		}
		if (-relativeMove.getZ() > highest) {
			highest = -relativeMove.getZ();
			direction = NORTH;
		}

		if (relativeMove.getY() > highest) {
			highest = relativeMove.getY();
			direction = UP;
		}
		if (-relativeMove.getY() > highest) {
			highest = -relativeMove.getY();
			direction = DOWN;
		}

		//		if (direction == NONE) {
		if (Math.abs(relativeMove.getYaw()) > highest) {
			highest = Math.abs(relativeMove.getYaw());
			direction = YAW;
		}

		if (Math.abs(relativeMove.getPitch()) > highest) {
			highest = Math.abs(relativeMove.getPitch());
			direction = PITCH;
		}
		//		}

		return direction;
	}

	private static int floor_double(double par0) {
		int var2 = (int) par0;
		return par0 < var2 ? var2 - 1 : var2;
	}
}
