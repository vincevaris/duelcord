package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameObject;

public abstract class Buff implements GameObject {
	private final Game.Member source;
	private int turns;
	private float power = 0;

	public Buff(Game.Member source, int turns) {
		this.source = source;
		this.turns = turns;
	}

	public Buff(Game.Member source, int turns, float power) {
		this(source, turns);
		this.power = power;
	}

	public Game.Member getSource() {
		return source;
	}

	public int getTurns() {
		return turns;
	}

	public int turn() {
		turns--;
		return turns;
	}

	public abstract String getName();

	public float getPower() {
		return power;
	}
}
