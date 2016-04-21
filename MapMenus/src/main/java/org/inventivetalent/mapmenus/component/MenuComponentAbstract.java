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
import lombok.NonNull;
import lombok.ToString;
import org.inventivetalent.mapmenus.IClickable;
import org.inventivetalent.mapmenus.ITickable;
import org.inventivetalent.mapmenus.bounds.FixedBounds;
import org.inventivetalent.mapmenus.render.IRenderable;

import java.util.UUID;

@EqualsAndHashCode(exclude = { "parentBounds" },
				   doNotUseGetters = true)
@ToString(exclude = { "parentBounds" },
		  doNotUseGetters = true)
public abstract class MenuComponentAbstract implements IRenderable, ITickable, IClickable {

	@Expose protected final UUID        uuid;
	protected final         FixedBounds parentBounds;
	@Expose protected       FixedBounds bounds;

	public MenuComponentAbstract(@NonNull UUID uuid, @NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds) {
		this.uuid = uuid;
		this.parentBounds = parentBounds;
		setBounds(bounds);
	}

	public MenuComponentAbstract(@NonNull FixedBounds parentBounds, @NonNull FixedBounds bounds) {
		this(UUID.randomUUID(), parentBounds, bounds);
	}

	/**
	 * @return The unique ID of this component
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * @return bounds of the component (Must be inside of the containing menu's bounds)
	 */
	public FixedBounds getBounds() {
		return bounds;
	}

//	public void setBounds(int x, int y, int width, int height) {
//		setBounds(new FixedBounds(x, y, width, height));
//	}

	// Method with doubles, so you don't have to always parse all numbers to integers
	public void setBounds(double x, double y, double width, double height) {
		setBounds(new FixedBounds((int) x, (int) y, (int) width, (int) height));
	}

	public void setBounds(@NonNull FixedBounds bounds) {
		this.bounds = bounds;
	}

	/**
	 * Moves the component to a new point
	 */
	public void move(int x, int y) {
		setBounds(new FixedBounds(x, y, getBounds().getWidth(), getBounds().getHeight()));
	}

	/**
	 * Moves the component relative to its current position
	 */
	public void moveRelative(int x, int y) {
		move((getBounds().getX() + x), (getBounds().getY() + y));
	}

}
