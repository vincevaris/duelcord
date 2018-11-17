package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.GameObject;

public abstract class Buff implements GameObject {
	public abstract String getName();

	public float getPower() {
		return 0;
	}
}
