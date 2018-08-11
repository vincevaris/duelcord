package com.oopsjpeg.enigma.game.effect.util;

import com.oopsjpeg.enigma.game.Stats;

public interface Effect {
	String getName();

	default float getPower() {
		return 0;
	}

	default Stats getStats() {
		return new Stats();
	}

	default Stats getPerTurn() {
		return new Stats();
	}

	default void onTurn() {

	}
}
