package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class IronScimitar extends Item {
	public static final String NAME = "Iron Scimitar";
	public static final int COST = 1225;
	public static final Item[] BUILD = new Item[]{new BronzeCutlass(), new Hatchet()};
	public static final Effect[] EFFECTS = new Effect[]{StatsEffect.stats(new Stats()
			.put(Stats.CRIT_DAMAGE, 0.5f))};
	public static final Stats STATS = new Stats()
			.put(Stats.DAMAGE, 25)
			.put(Stats.CRIT_CHANCE, 0.4f);

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