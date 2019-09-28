package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;

public class Warrior extends Unit {
	public static final String NAME = "Warrior";
	public static final String DESC = "Every **3rd** attack deals **30%** bonus damage."
			+ "\nUsing `>bash` destroys shields and deals **50%** of damage."
			+ "\n`>bash` counts towards bonus damage stacks, but does not proc it.";
	public static final Color COLOR = Color.CYAN;
	public static final Stats STATS = new Stats()
			.put(Stats.ENERGY, 125)
			.put(Stats.MAX_HP, 795)
			.put(Stats.DAMAGE, 24)
			.put(Stats.ABILITY_POWER, 1);
	public static final Stats PER_TURN = new Stats()
			.put(Stats.HP, 13)
			.put(Stats.GOLD, 75);

	private int bonus = 0;
	private boolean bash = false;

	public int getBonus() {
		return bonus;
	}

	public void setBonus(int bonus) {
		this.bonus = Util.limit(bonus, 0, 3);
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
	public String onTurnEnd(Game.Member member) {
		bash = false;
		return "";
	}

	@Override
	public DamageEvent onBasicAttack(DamageEvent event) {
		if (bonus() >= 3) {
			event.bonus += event.damage * 0.3f * event.actor.getStats().get(Stats.ABILITY_POWER);
			setBonus(0);
		}

		return event;
	}
}
