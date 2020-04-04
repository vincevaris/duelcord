package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.unit.*;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public abstract class Unit extends GameObject {
    private final Command[] commands;
    private final Color color;
    private final Stats stats;

    public Unit(String name, Command[] commands, Color color, Stats stats) {
        super(name);
        this.commands = commands;
        this.color = color;
        this.stats = stats;
    }

    private static final Unit[] values = {
            new Berserker(), new Thief(), new Warrior(),
            new Duelist(), new Gunslinger(), new Assassin(),
            new Phasebreaker(), new Blademaster(), new Bloodreaper()
    };

    public static Unit[] values() {
        return values;
    }

    public static Unit fromName(String name) {
        if (name.equalsIgnoreCase("random"))
            return values()[Util.RANDOM.nextInt(values().length)];
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

    public Command[] getCommands() {
        return commands != null ? commands : new Command[0];
    }

    public Color getColor() {
        return color;
    }

    public Stats getStats() {
        return stats;
    }

    @Override
    public String toString() {
        return getName();
    }
}
