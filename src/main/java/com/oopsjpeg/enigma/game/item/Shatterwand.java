package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class Shatterwand extends Item {
	public static final String NAME = "Shatterwand";
	public static final int COST = 1200;
	public static final Effect[] EFFECTS = new Effect[]{StatsEffect.stats(new Stats()
			.put(Stats.ABILITY_POWER, 0.25f))};

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getCost() {
		return COST;
	}

	@Override
	public boolean canUse() {
		return true;
	}

	@Override
	public String onUse(Game.Member member) {
		return null; //TODO implement
	}
}
