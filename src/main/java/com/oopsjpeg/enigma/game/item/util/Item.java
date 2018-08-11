package com.oopsjpeg.enigma.game.item.util;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.util.Effect;
import com.oopsjpeg.enigma.game.item.*;

import java.lang.reflect.InvocationTargetException;

public interface Item {
	Item[] values = {
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

	static Item[] values() {
		return values;
	}

	static Item fromName(String name) {
		for (Item i : values)
			if (name.equalsIgnoreCase(i.getName()) || (name.length() >= 3
					&& name.toLowerCase().startsWith(i.getName().toLowerCase()))) {
				try {
					return i.getClass().getConstructor().newInstance();
				} catch (IllegalAccessException | InstantiationException
						| NoSuchMethodException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		return null;
	}

	String getName();

	int getCost();

	default Item[] getBuild() {
		return new Item[0];
	}

	default Effect[] getEffects() {
		return new Effect[0];
	}

	default Stats getStats() {
		return new Stats();
	}

	default Stats getPerTurn() {
		return new Stats();
	}

	default boolean canUse() {
		return false;
	}

	default boolean removeOnUse() {
		return true;
	}

	default String onUse(Game.Member user) {
		return "";
	}
}
