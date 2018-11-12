package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.effect.util.Effect;

public class LoveOfWar implements Effect {
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
	public String getName() {
		return NAME;
	}

	@Override
	public String getDesc() {
		return "Each attack increases damage dealt by **" + Math.round(power * 100) + "%** for that turn.";
	}

	@Override
	public float getPower() {
		return power;
	}

	@Override
	public String onTurnEnd(Game.Member user) {
		attack = 0;
		return "";
	}
}
