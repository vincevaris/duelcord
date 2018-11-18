package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.unit.*;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public abstract class Unit implements GameObject {
	private static final Unit[] values = {
			new Berserker(), new Thief(), new Warrior(),
			new Gunslinger(), new Duelist()
	};

	public static Unit[] values() {
		return values;
	}

	public static Unit fromName(String name) {
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

	public abstract String getName();

	public abstract String getDesc();

	public abstract Color getColor();

	public boolean isRanged() {
		return false;
	}

	public abstract Stats getStats();

	public abstract Stats getPerTurn();
}
