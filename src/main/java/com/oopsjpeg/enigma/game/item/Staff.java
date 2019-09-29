package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;

public class Staff extends Item {
    public static final String NAME = "Staff";
    public static final int COST = 450;
    public static final Stats STATS = new Stats()
            .put(Stats.ABILITY_POWER, 20);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getCost() {
        return COST;
    }

    @Override
    public Stats getStats() {
        return STATS;
    }
}
