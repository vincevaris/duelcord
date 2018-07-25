package com.oopsjpeg.enigma.game.units;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.units.util.Unit;

import java.awt.*;

public class ThiefUnit extends Unit {
	public static final String NAME = "Thief";
	public static final String DESC =
			"The first critical strike in a turn steals gold equal to **40%** of damage.\n" +
			"Base crit damage is reduced by **20%**, however, subsequent crits in a turn deal increasing damage.";
	public static final Color COLOR = Color.YELLOW;
	public static final Stats STATS = new Stats();
	public static final Stats PER_TURN = new Stats();

	static {
		STATS.energy = 150;
		STATS.maxHp = 525;
		STATS.damage = 18;
		STATS.critChance = 0.15f;
		STATS.critDamage = -0.2f;
		PER_TURN.hp = 8;
		PER_TURN.gold = 50;
	}

	private int crit = 0;

	public int getCrit() {
		return crit;
	}

	public void setCrit(int crit) {
		this.crit = crit;
	}

	public int crit() {
		crit++;
		return crit;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDesc() {
		return DESC;
	}

	@Override
	public Color getColor() {
		return COLOR;
	}

	@Override
	public Stats getStats() {
		return STATS;
	}

	@Override
	public Stats getPerTurn() {
		return PER_TURN;
	}

	@Override
	public String onTurn() {
		crit = 0;
		return "";
	}
}
