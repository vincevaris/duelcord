package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;

public class Hatchet extends Item {
    public static final String NAME = "Hatchet";
    public static final int COST = 425;
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 10);

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
