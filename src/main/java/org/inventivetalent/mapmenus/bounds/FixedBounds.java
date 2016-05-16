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

package org.inventivetalent.mapmenus.bounds;

import com.google.gson.annotations.Expose;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.inventivetalent.mapmenus.menu.CursorPosition;

import java.awt.*;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class FixedBounds implements IBounds {

	@Expose public int x, y, width, height;
	Rectangle rectangle;

	public FixedBounds(Rectangle bounds) {
		this.rectangle = bounds;
		this.x = (int) bounds.getX();
		this.y = (int) bounds.getY();
		this.width = (int) bounds.getWidth();
		this.height = (int) bounds.getHeight();
	}

	public FixedBounds(IBounds iBounds) {
		this(new Rectangle(iBounds.getX(), iBounds.getY(), iBounds.getWidth(), iBounds.getHeight()));
	}

	public FixedBounds(int x, int y, int width, int height) {
		this(new Rectangle(x, y, width, height));
	}

	public FixedBounds(int[] array) {
		this(array[0], array[1], array[2], array[3]);
	}

	Rectangle getRectangle() {
		if (rectangle != null) { return rectangle; }
		return rectangle = new Rectangle(getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public boolean contains(int x, int y) {
		return getRectangle().contains(x, y);
	}

	@Override
	public boolean contains(int x, int y, int width, int height) {
		return getRectangle().contains(x, y, width, height);
	}

	@Override
	public boolean contains(IBounds bounds) {
		return getRectangle().contains(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

	public boolean contains(CursorPosition position) {
		return contains(position.getX(), position.getY());
	}

	public int[] toIntArray() {
		return new int[] {
				getX(),
				getY(),
				getWidth(),
				getHeight() };
	}

}
