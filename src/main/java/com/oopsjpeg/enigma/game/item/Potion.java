package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.item.util.Item;

public class Potion implements Item {
	public static final String NAME = "Potion";
	public static final int COST = 50;

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
	public String onUse(Game.Member user) {
		return user.heal(50);
	}
}
