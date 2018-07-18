package com.oopsjpeg.enigma.game.items;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.items.util.Item;

public class BronzeCutlass extends Item {
    public static final String NAME = "Bronze Cutlass";
    public static final int COST = 575;
    public static final Stats STATS = new Stats();
    public static final Item[] BUILD = new Item[]{new Knife()};

    static {
        STATS.damage = 12;
        STATS.critChance = 0.2f;
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
    public Stats getStats() {
        return STATS;
    }
}
