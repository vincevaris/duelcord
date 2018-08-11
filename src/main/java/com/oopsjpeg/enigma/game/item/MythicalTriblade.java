package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.Bruiser;
import com.oopsjpeg.enigma.game.effect.LoveOfWar;
import com.oopsjpeg.enigma.game.effect.util.Effect;
import com.oopsjpeg.enigma.game.item.util.Item;

public class MythicalTriblade implements Item {
	public static final String NAME = "Mythical Triblade";
	public static final int COST = 1250;
	public static final Item[] BUILD = new Item[]{new BronzeCutlass(), new MidnightDagger()};
	public static final Effect[] EFFECTS = new Effect[]{new LoveOfWar(0.2f), new Bruiser(0.2f)};
	public static final Stats STATS = new Stats();

	static {
		STATS.damage = 22;
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
