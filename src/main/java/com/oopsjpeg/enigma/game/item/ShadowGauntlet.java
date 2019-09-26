package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.LoveOfWar;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class ShadowGauntlet extends Item {
	public static final String NAME = "Shadow Gauntlet";
	public static final int COST = 1075;
	public static final Item[] BUILD = new Item[]{new MidnightDagger(), new Ring()};
	public static final Effect[] EFFECTS = new Effect[]{new LoveOfWar(0.2f)};
	public static final Stats STATS = new Stats()
			.put(Stats.DAMAGE, 10)
			.put(Stats.ABILITY_POWER, 0.15f);

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getCost() {
		return COST;
	}

	@Override
	public Item[] getBuild() {
		return BUILD;
	}

	@Override
	public Effect[] getEffects() {
		return EFFECTS;
	}

	@Override
	public Stats getStats() {
		return STATS;
	}
}