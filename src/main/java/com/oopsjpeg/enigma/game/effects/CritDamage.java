package com.oopsjpeg.enigma.game.effects;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effects.util.Effect;

public class CritDamage extends Effect {
	public static final String NAME = "Critical Damage";
	private final float power;
	private final Stats stats = new Stats();

	public CritDamage(float power) {
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
		stats.critDamage = power;
		return stats;
	}
}
