package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.unit.*;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.oopsjpeg.enigma.game.Stats.*;
import static com.oopsjpeg.enigma.game.Stats.LIFE_STEAL;

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
            new Phasebreaker(), new Bloodreaper(),
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

    public EmbedCreateSpec format() {
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();

        embed.color(getColor());
        embed.description("## " + getName() + "\n" + getDescription() + "\n\u1CBC\u1CBC");

        for (Command cmd : getCommands())
            embed.addField("`>" + cmd.getName() + "`", cmd.getDescription(), false);

        return embed.build();
    }

    public EmbedCreateSpec formatStats() {
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        List<String> desc = new ArrayList<>();

        desc.add("## " + getName() + " Stats");
        desc.add("Health: **" + stats.getInt(MAX_HEALTH) + "** (+**" + stats.getInt(HEALTH_PER_TURN) + "**/turn)");
        desc.add("Damage: **" + stats.getInt(DAMAGE) + "**");
        desc.add("Energy: **" + stats.getInt(MAX_ENERGY) + "**");
        if (stats.get(CRIT_CHANCE) > 0)
            desc.add("Critical Chance: **" + Util.percent(stats.get(CRIT_CHANCE)) + "**");
        if (stats.get(LIFE_STEAL) > 0)
            desc.add("Life Steal: **" + Util.percent(stats.get(LIFE_STEAL)) + "**");

        embed.color(getColor());
        embed.description(String.join("\n", desc));

        return embed.build();
    }

    @Override
    public String toString() {
        return getName();
    }
}
