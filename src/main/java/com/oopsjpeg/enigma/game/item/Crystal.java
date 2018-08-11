package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.item.util.Item;

public class Crystal implements Item {
	public static final String NAME = "Crystal";
	public static final int COST = 200;
	public static final Stats STATS = new Stats();

	static {
		STATS.maxHp = 20;
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
