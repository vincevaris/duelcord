package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.unit.util.Unit;

import java.awt.*;

public class WarriorUnit implements Unit {
	public static final String NAME = "Warrior";
	public static final String DESC = "Every third attack deals **25%** more damage.\n"
			+ "Using `>bash` destroys shields and deals **40%** of damage.";
	public static final Color COLOR = Color.CYAN;
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
		this.bonus = Math.max(0, Math.min(3, bonus));
	}

	public int bonus() {
		setBonus(bonus + 1);
		return bonus;
	}

	public boolean getBash() {
		return bash;
	}

	public void setBash(boolean bash) {
		this.bash = bash;
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
		bash = false;
		return "";
	}
}
