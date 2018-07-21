package com.oopsjpeg.enigma.game.items.util;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effects.util.Effect;
import com.oopsjpeg.enigma.game.items.*;

import java.lang.reflect.InvocationTargetException;

public abstract class Item {
	private static final Item[] values = {
			new AuroralMask(),
			new BloodlustBlade(),
			new BronzeCutlass(),
			new Crystal(),
			new Hatchet(),
			new IronScimitar(),
			new Knife(),
			new MidnightDagger(),
			new MythicalTriblade(),
			new Potion(),
			new Soulstealer()
	};

	public static Item fromName(String name) {
		for (Item i : values)
			if (i.getName().toLowerCase().startsWith(name.toLowerCase())) try {
				return (Item) i.getClass().getConstructors()[0].newInstance();
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException ignore) {
			}
		return null;
	}

	public abstract String getName();

	public abstract int getCost();

	public Item[] getBuild() {
		return new Item[0];
	}

	public Effect[] getEffects() {
		return new Effect[0];
	}

	public Stats getStats() {
		return new Stats();
	}

	public Stats getPerTurn() {
		return new Stats();
	}

	public boolean canUse() {
		return false;
	}

	public boolean removeOnUse() {
		return true;
	}

	public String onUse(Game.Member user) {
		return "";
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass().equals(this.getClass());
	}
}
