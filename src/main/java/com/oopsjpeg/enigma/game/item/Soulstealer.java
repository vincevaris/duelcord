package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class Soulstealer extends Item {
	public static final String NAME = "Soulstealer";
	public static final int COST = 1275;
	public static final Item[] BUILD = new Item[]{new BloodlustBlade(), new Hatchet()};
	public static final Effect[] EFFECTS = new Effect[]{StatsEffect.stats(new Stats()
			.put(Stats.LIFE_STEAL, 0.2f))};
	public static final Stats STATS = new Stats()
			.put(Stats.DAMAGE, 25);

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
