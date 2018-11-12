package com.oopsjpeg.enigma.game.effect.util;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;

public interface Effect {
	String getName();

	String getDesc();

	default float getPower() {
		return 0;
	}

	default Stats getStats() {
		return new Stats();
	}

	default Stats getPerTurn() {
		return new Stats();
	}

	default String onTurnStart(Game.Member user) {
		return "";
	}

	default String onTurnEnd(Game.Member user) {
		return "";
	}
}
