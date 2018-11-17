package com.oopsjpeg.enigma.game;

public interface GameObject {
	default String onTurnStart(Game.Member user) {
		return "";
	}

	default String onTurnEnd(Game.Member user) {
		return "";
	}

	default String onDefend(Game.Member member) {
		return "";
	}
}
