package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Emote;

import java.awt.*;

public class Assassin extends Unit {
	public static final String NAME = "Assassin";
	public static final String DESC = "**20%** of damage dealt in the last turn is stored as **Potency**."
			+ " This can only occur **5** times until **Potency** is reset."
			+ "\n\nUsing `>slash` deals **25%** of base damage. Every fourth `>slash` **silences** the enemy"
			+ " for **1** turn and deals bonus damage equal to the total **Potency**, resetting it as well."
			+ "\n\n`>slash` does not count towards total **Potency**.";
	public static final Color COLOR = Color.BLUE;
	public static final Stats STATS = new Stats()
			.put(Stats.ENERGY, 125)
			.put(Stats.MAX_HP, 720)
			.put(Stats.DAMAGE, 22);
	public static final Stats PER_TURN = new Stats()
			.put(Stats.HP, 11)
			.put(Stats.GOLD, 75);

	private boolean slashed = false;
	private int slashCount = 0;
	private float potency = 0;
	private float potencyNow = 0;
	private int potencyTurns = 0;

	public boolean getSlashed() {
		return slashed;
	}

	public void setSlashed(boolean slashed) {
		this.slashed = slashed;
	}

	public int getSlashCount() {
		return slashCount;
	}

	public void setSlashCount(int slashCount) {
		this.slashCount = Math.max(0, Math.min(4, slashCount));
	}

	public int slashCount() {
		slashCount++;
		return slashCount;
	}

	public float getPotency() {
		return potency;
	}

	public void setPotency(float potency) {
		this.potency = potency;
	}

	public void addPotency(float potency) {
		this.potency += potency;
	}

	public float getPotencyNow() {
		return potencyNow;
	}

	public void setPotencyNow(float potencyNow) {
		this.potencyNow = potencyNow;
	}

	public void addPotencyNow(float potencyNow) {
		this.potencyNow += potencyNow;
	}

	public int getPotencyTurns() {
		return potencyTurns;
	}

	public void setPotencyTurns(int potencyTurns) {
		this.potencyTurns = potencyTurns;
	}

	@Override
	public String onTurnStart(Game.Member member) {
		slashed = false;
		if (potencyNow > 0) {
			potencyNow = 0;
			potencyTurns++;
		}
		if (potencyTurns == 5)
			return Emote.KNIFE + "**" + member.getName() + "'s Potency** is at maximum capacity.";
		return "";
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
