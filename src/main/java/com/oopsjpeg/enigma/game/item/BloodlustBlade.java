package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.util.Item;
import com.oopsjpeg.enigma.game.util.Stats;

public class BloodlustBlade extends Item {
	public static final String NAME = "Bloodlust Blade";
	public static final int COST = 550;
	public static final Stats STATS = new Stats()
			.put(Stats.DAMAGE, 10)
			.put(Stats.LIFE_STEAL, 0.1f);
	public static final Item[] BUILD = new Item[]{new Knife()};

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
	public Stats getStats() {
		return STATS;
	}
}
