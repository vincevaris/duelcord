package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.unit.util.Unit;

import java.awt.*;

public class ThiefUnit implements Unit {
	public static final String NAME = "Thief";
	public static final String DESC = "The first critical strike in a turn steals gold equal to **40%** of damage."
			+ "\nBase crit damage is reduced by **20%**, however, subsequent crits in a turn deal increasing damage.";
	public static final Color COLOR = Color.YELLOW;
	public static final Stats STATS = new Stats()
			.put(Stats.ENERGY, 150)
			.put(Stats.MAX_HP, 525)
			.put(Stats.DAMAGE, 18)
			.put(Stats.CRIT_CHANCE, 0.2f)
			.put(Stats.CRIT_DAMAGE, -0.2f);
	public static final Stats PER_TURN = new Stats()
			.put(Stats.HP, 8)
			.put(Stats.GOLD, 75);

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
