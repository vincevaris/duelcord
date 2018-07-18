package com.oopsjpeg.enigma.game.items;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effects.LoveOfWar;
import com.oopsjpeg.enigma.game.effects.util.Effect;
import com.oopsjpeg.enigma.game.items.util.Item;

public class MidnightDagger extends Item {
    public static final String NAME = "Midnight Dagger";
    public static final int COST = 425;
    public static final Item[] BUILD = new Item[]{new Knife()};
    public static final Effect[] EFFECTS = new Effect[]{new LoveOfWar(1)};
    public static final Stats STATS = new Stats();

    static {
        STATS.damage = 8;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getCost() {
        return COST;
    }

    @Override
    public Item[] getBuild() {
        return BUILD;
    }

    @Override
    public Effect[] getEffects() {
        return EFFECTS;
    }

    @Override
    public Stats getStats() {
        return STATS;
    }
}
