package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.DawnShield;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class DawnHammer extends Item {
	public static final String NAME = "Dawn Hammer";
	public static final int COST = 1025;
	public static final Item[] BUILD = new Item[]{new SteelMallet(), new Knife()};
	public static final Effect[] EFFECTS = new Effect[]{StatsEffect.perTurn(new Stats()
			.put(Stats.ENERGY, 25)), new DawnShield()};
	public static final Stats STATS = new Stats()
			.put(Stats.DAMAGE, 15)
			.put(Stats.MAX_HP, 40);

	public String getName() {
		return NAME;
	}

	public int getCost() {
		return COST;
	}

	public Item[] getBuild() {
		return BUILD;
	}

	@Override
	public Effect[] getEffects() {
		return EFFECTS;
	}

	public Stats getStats() {
		return STATS;
	}
}
