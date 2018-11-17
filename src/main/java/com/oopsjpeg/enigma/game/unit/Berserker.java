package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.util.Stats;
import com.oopsjpeg.enigma.game.util.Unit;
import com.oopsjpeg.enigma.util.Emote;

import java.awt.*;

public class Berserker implements Unit {
	public static final String NAME = "Berserker";
	public static final String DESC = "Attacking or being attacked builds up to **5** stacks of **Rage**."
			+ "\nUsing `>rage` consumes stacks to increase damage for a single turn (**4%** per stack)."
			+ "\nAt maximum stacks, `>rage` grants **100** bonus energy.";
	public static final Color COLOR = Color.RED;
	public static final Stats STATS = new Stats()
			.put(Stats.ENERGY, 100)
			.put(Stats.MAX_HP, 560)
			.put(Stats.DAMAGE, 19);
	public static final Stats PER_TURN = new Stats()
			.put(Stats.HP, 12)
			.put(Stats.GOLD, 75);

	private int rage = 0;
	private float bonus = 0;

	public int getRage() {
		return rage;
	}

	public void setRage(int rage) {
		this.rage = Math.max(0, Math.min(5, rage));
	}

	public float getBonus() {
		return bonus;
	}

	public void setBonus(float bonus) {
		this.bonus = bonus;
	}

	public int rage() {
		setRage(rage + 1);
		return rage;
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
	public String onAttack(Game.Member member) {
		return stack(member);
	}

	@Override
	public String onAttacked(Game.Member member) {
		return stack(member);
	}

	public String stack(Game.Member member) {
		if (rage == 5) {
			rage();
			return Emote.RAGE + "**" + member.getName() + "'s Rage** is at maximum capacity.\n";
		}
		return "";
	}
}
