package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.Vampirism;
import com.oopsjpeg.enigma.game.effect.util.Effect;
import com.oopsjpeg.enigma.game.item.util.Item;

public class Soulstealer implements Item {
	public static final String NAME = "Soulstealer";
	public static final int COST = 1275;
	public static final Item[] BUILD = new Item[]{new BloodlustBlade(), new Hatchet()};
	public static final Effect[] EFFECTS = new Effect[]{new Vampirism(0.2f)};
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
