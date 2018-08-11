package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.util.Effect;

public class Bruiser implements Effect {
	public static final String NAME = "Bruiser";
	private final float power;
	private final Stats stats = new Stats();

	public Bruiser(float power) {
		this.power = power;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public float getPower() {
		return power;
	}

	@Override
	public Stats getStats() {
		stats.critChance = power;
		return stats;
	}
}
