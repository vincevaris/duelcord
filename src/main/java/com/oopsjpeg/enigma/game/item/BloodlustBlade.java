package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.item.util.Item;

public class BloodlustBlade implements Item {
	public static final String NAME = "Bloodlust Blade";
	public static final int COST = 550;
	public static final Stats STATS = new Stats();
	public static final Item[] BUILD = new Item[]{new Knife()};

	static {
		STATS.damage = 10;
		STATS.lifeSteal = 0.1f;
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
	public Item[] getBuild() {
		return BUILD;
	}

	@Override
	public Stats getStats() {
		return STATS;
	}
}
