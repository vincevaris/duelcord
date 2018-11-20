package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;

import java.awt.*;

public class Duelist extends Unit {
	public static final String NAME = "Duelist";
	public static final String DESC = "Every **5th** attack deals bonus damage equal to **4%** of the"
			+ " target's max health and applies **Bleed** by **40%** of your damage for **2** turns.";
	public static final Color COLOR = Color.MAGENTA;
	public static final Stats STATS = new Stats()
			.put(Stats.ENERGY, 125)
			.put(Stats.MAX_HP, 770)
			.put(Stats.DAMAGE, 25);
	public static final Stats PER_TURN = new Stats()
			.put(Stats.HP, 14)
			.put(Stats.GOLD, 75);

	private int attack = 0;

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = Math.max(0, Math.min(5, attack));
	}

	public int attack() {
		setAttack(attack + 1);
		return attack;
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
}
