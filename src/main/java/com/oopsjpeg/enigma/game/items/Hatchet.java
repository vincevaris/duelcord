package com.oopsjpeg.enigma.game.items;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.items.util.Item;

public class Hatchet extends Item {
	public static final String NAME = "Hatchet";
	public static final int COST = 375;
	public static final Stats STATS = new Stats();

	static {
		STATS.damage = 10;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getCost() {
		return COST;
	}

	@Override
	public Stats getStats() {
		return STATS;
	}
}
