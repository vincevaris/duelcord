package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.Bruiser;
import com.oopsjpeg.enigma.game.effect.CritDamage;
import com.oopsjpeg.enigma.game.effect.util.Effect;
import com.oopsjpeg.enigma.game.item.util.Item;

public class IronScimitar implements Item {
	public static final String NAME = "Iron Scimitar";
	public static final int COST = 1275;
	public static final Item[] BUILD = new Item[]{new BronzeCutlass(), new Hatchet()};
	public static final Effect[] EFFECTS = new Effect[]{new CritDamage(0.5f), new Bruiser(0.4f)};
	public static final Stats STATS = new Stats();

	static {
		STATS.damage = 25;
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
	public Effect[] getEffects() {
		return EFFECTS;
	}

	@Override
	public Stats getStats() {
		return STATS;
	}
}