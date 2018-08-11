package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.item.util.Item;

public class Hatchet implements Item {
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
