package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.util.Item;
import com.oopsjpeg.enigma.game.util.Stats;

public class Crystal extends Item {
	public static final String NAME = "Crystal";
	public static final int COST = 200;
	public static final Stats STATS = new Stats()
			.put(Stats.MAX_HP, 20);

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
