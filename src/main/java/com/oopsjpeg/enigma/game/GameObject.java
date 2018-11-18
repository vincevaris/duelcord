package com.oopsjpeg.enigma.game;

public interface GameObject {
	default String onTurnStart(Game.Member member) {
		return "";
	}

	default String onTurnEnd(Game.Member member) {
		return "";
	}

	default String onDefend(Game.Member member) {
		return "";
	}
}
