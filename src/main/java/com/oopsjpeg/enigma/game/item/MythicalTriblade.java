package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.effect.LoveOfWar;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.util.Effect;
import com.oopsjpeg.enigma.game.util.Item;
import com.oopsjpeg.enigma.game.util.Stats;

public class MythicalTriblade extends Item {
	public static final String NAME = "Mythical Triblade";
	public static final int COST = 1250;
	public static final Item[] BUILD = new Item[]{new BronzeCutlass(), new MidnightDagger()};
	public static final Effect[] EFFECTS = new Effect[]{StatsEffect.stats(new Stats()
			.put(Stats.CRIT_CHANCE, 0.2f)),
			new LoveOfWar(0.2f)};
	public static final Stats STATS = new Stats()
			.put(Stats.DAMAGE, 22);

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
