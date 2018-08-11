package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.unit.util.Unit;

import java.awt.*;

public class ThiefUnit implements Unit {
	public static final String NAME = "Thief";
	public static final String DESC = "The first critical strike in a turn steals gold equal to **40%** of damage.\n"
			+ "Base crit damage is reduced by **20%**, however, subsequent crits in a turn deal increasing damage.";
	public static final Color COLOR = Color.YELLOW;
	public static final Stats STATS = new Stats();
	public static final Stats PER_TURN = new Stats();

	static {
		STATS.energy = 150;
		STATS.maxHp = 525;
		STATS.damage = 18;
		STATS.critChance = 0.2f;
		STATS.critDamage = -0.2f;
		PER_TURN.hp = 8;
		PER_TURN.gold = 50;
	}

	private int crit = 0;

	public int getCrit() {
		return crit;
	}

	public void setCrit(int crit) {
		this.crit = Math.max(0, crit);
	}

	public int crit() {
		setCrit(crit + 1);
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
	public String onTurn(Game.Member member) {
		crit = 0;
		return "";
	}
}
