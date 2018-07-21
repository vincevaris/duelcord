package com.oopsjpeg.enigma.game.effects;

import com.oopsjpeg.enigma.game.effects.util.Effect;

public class LoveOfWar extends Effect {
	public static final String NAME = "Love of War";
	private final float power;

	private int attack = 0;

	public LoveOfWar(float power) {
		this.power = power;
	}

	public int attack() {
		attack++;
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	@Override
	public float getPower() {
		return power;
	}

	@Override
	public void onTurn() {
		attack = 0;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
