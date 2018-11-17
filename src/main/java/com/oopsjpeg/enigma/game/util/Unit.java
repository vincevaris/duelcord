package com.oopsjpeg.enigma.game.util;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.unit.Berserker;
import com.oopsjpeg.enigma.game.unit.Gunslinger;
import com.oopsjpeg.enigma.game.unit.Thief;
import com.oopsjpeg.enigma.game.unit.Warrior;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public interface Unit {
	Unit[] values = {
			new Berserker(), new Thief(), new Warrior(),
			new Gunslinger()
	};

	static Unit[] values() {
		return values;
	}

	static Unit fromName(String name) {
		for (Unit u : values)
			if (name.equalsIgnoreCase(u.getName()) || (name.length() >= 3
					&& u.getName().toLowerCase().startsWith(name.toLowerCase()))) {
				try {
					return u.getClass().getConstructor().newInstance();
				} catch (IllegalAccessException | InstantiationException
						| NoSuchMethodException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		return null;
	}

	String getName();

	String getDesc();

	Color getColor();

	default boolean isRanged() {
		return false;
	}

	Stats getStats();

	Stats getPerTurn();

	default String onTurnStart(Game.Member member) {
		return "";
	}

	default String onTurnEnd(Game.Member member) {
		return "";
	}

	default String onDefend(Game.Member member) {
		return "";
	}
}
