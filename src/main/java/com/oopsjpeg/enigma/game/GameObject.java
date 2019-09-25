package com.oopsjpeg.enigma.game;

public abstract class GameObject {
	public String onTurnStart(Game.Member member) {
		return "";
	}

	public String onTurnEnd(Game.Member member) {
		return "";
	}

	public String onDefend(Game.Member member) {
		return "";
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass().equals(this.getClass());
	}
}
