package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.util.Stats;
import com.oopsjpeg.enigma.game.effect.DawnShield;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.util.Effect;
import com.oopsjpeg.enigma.game.util.Item;

public class AuroralMask extends Item {
	public static final String NAME = "Auroral Mask";
	public static final int COST = 750;
	public static final Item[] BUILD = new Item[]{new Crystal(), new Crystal()};
	public static final Effect[] EFFECTS = new Effect[]{StatsEffect.perTurn(new Stats()
			.put(Stats.ENERGY, 25)), new DawnShield()};
	public static final Stats STATS = new Stats()
			.put(Stats.HP, 80.0f);

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
