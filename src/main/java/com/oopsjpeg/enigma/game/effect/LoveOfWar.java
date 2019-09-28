package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

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
	public String getName() {
		return NAME;
	}

	@Override
	public String getDesc() {
		return "Each attack increases damage dealt by **" + Util.percent(power) + "** for that turn.";
	}

	@Override
	public float getPower() {
		return power;
	}

	@Override
	public String onTurnEnd(Game.Member member) {
		attack = 0;
		return "";
	}

	@Override
	public DamageEvent onHit(DamageEvent event) {
		event.damage *= 1 + ((attack() - 1) * power);
		return event;
	}
}
