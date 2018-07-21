package com.oopsjpeg.enigma.game.units.util;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.units.BerserkerUnit;
import com.oopsjpeg.enigma.game.units.ThiefUnit;
import com.oopsjpeg.enigma.game.units.WarriorUnit;

import java.lang.reflect.InvocationTargetException;

public abstract class Unit {
	private static final Unit[] values = {
			new BerserkerUnit(),
			new ThiefUnit(),
			new WarriorUnit()
	};

	public static Unit fromName(String name) {
		for (Unit u : values)
			if (u.getName().equalsIgnoreCase(name)) try {
				return (Unit) u.getClass().getConstructors()[0].newInstance();
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException ignore) {
			}
		return null;
	}

	public abstract String getName();

	public abstract Stats getStats();

	public abstract Stats getPerTurn();

	public String onTurn() {
		return "";
	}

	public String onDefend() {
		return "";
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass().equals(this.getClass());
	}
}
