package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;

public class BronzeCutlass extends Item {
	public static final String NAME = "Bronze Cutlass";
	public static final int COST = 575;
	public static final Stats STATS = new Stats()
			.put(Stats.DAMAGE, 12)
			.put(Stats.CRIT_CHANCE, 0.2f);
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
