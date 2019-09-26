package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Emote;

import java.awt.*;

public class Berserker extends Unit {
	public static final String NAME = "Berserker";
	public static final String DESC = "Attacking or being attacked builds up to **5** stacks of **Rage**."
			+ "\nUsing `>rage` consumes stacks to increase damage for a single turn (**4%** per stack)."
			+ "\nAt maximum stacks, `>rage` grants **100** bonus energy.";
	public static final Color COLOR = Color.RED;
	public static final Stats STATS = new Stats()
			.put(Stats.ENERGY, 100)
			.put(Stats.MAX_HP, 780)
			.put(Stats.DAMAGE, 19)
			.put(Stats.ABILITY_POWER, 1);
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
	public String onTurnStart(Game.Member member) {
		return stack(member);
	}

	@Override
	public String onTurnEnd(Game.Member member) {
		bonus = 0;
		return "";
	}

	public String stack(Game.Member member) {
		if (rage == 5) {
			rage();
			return Emote.RAGE + "**" + member.getName() + "'s Rage** is at maximum capacity.";
		}
		return "";
	}
}
