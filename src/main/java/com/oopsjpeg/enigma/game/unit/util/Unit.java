package com.oopsjpeg.enigma.game.unit.util;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.unit.BerserkerUnit;
import com.oopsjpeg.enigma.game.unit.ThiefUnit;
import com.oopsjpeg.enigma.game.unit.WarriorUnit;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public interface Unit {
	Unit[] values = {
			new BerserkerUnit(), new ThiefUnit(), new WarriorUnit()
	};

	static Unit[] values() {
		return values;
	}

	static Unit fromName(String name) {
		for (Unit u : values)
			if (u.getName().toLowerCase().startsWith(name.toLowerCase()))
				try {
					return u.getClass().getConstructor().newInstance();
				} catch (NoSuchMethodException | IllegalAccessException
						| InstantiationException | InvocationTargetException ignored) {

				}
		return null;
	}

	String getName();

	String getDesc();

	Color getColor();

	Stats getStats();

	Stats getPerTurn();

	default String onTurn(Game.Member member) {
		return "";
	}

	default String onDefend(Game.Member member) {
		return "";
	}
}
