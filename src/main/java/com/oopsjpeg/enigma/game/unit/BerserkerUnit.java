package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.unit.util.Unit;
import com.oopsjpeg.enigma.util.Emote;

import java.awt.*;

public class BerserkerUnit implements Unit {
	public static final String NAME = "Berserker";
	public static final String DESC = "Being attacked and defending builds up to **6** stacks of **Rage**."
			+ "\nUsing `>rage` consumes stacks (min. 2) to increase energy for a turn (**25** per **2** stacks)."
			+ "\nUsing `>rage` at full capacity grants a bonus **50** energy.";
	public static final Color COLOR = Color.RED;
	public static final Stats STATS = new Stats()
			.put(Stats.ENERGY, 75)
			.put(Stats.MAX_HP, 610)
			.put(Stats.DAMAGE, 28);
	public static final Stats PER_TURN = new Stats()
			.put(Stats.HP, 15)
			.put(Stats.GOLD, 75);

	private int rage = 0;

	public int getRage() {
		return rage;
	}

	public void setRage(int rage) {
		this.rage = Math.max(0, Math.min(6, rage));
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
		if (rage() == 6)
			return Emote.RAGE + "**" + member.getName() + "'s Rage** is at maximum capacity.\n";
		return "";
	}
}
