package com.oopsjpeg.enigma.game.units;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.units.util.Unit;

public class WarriorUnit extends Unit {
	public static final String NAME = "Warrior";
	public static final Stats STATS = new Stats();
	public static final Stats PER_TURN = new Stats();

	static {
		STATS.energy = 125;
		STATS.maxHp = 580;
		STATS.damage = 24;
		PER_TURN.hp = 13;
		PER_TURN.gold = 50;
	}

	private int bonus = 0;
	private boolean bash = false;

	public int getBonus() {
		return bonus;
	}

	public void setBonus(int bonus) {
		this.bonus = bonus;
	}

	public int bonus() {
		bonus++;
		return bonus;
	}

	public boolean getBash() {
		return bash;
	}

	public void setBash(boolean bash) {
		this.bash = bash;
	}

	@Override
	public String onTurn() {
		bash = false;
		return "";
	}

	@Override
	public String getName() {
		return NAME;
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
