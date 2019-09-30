package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;

public class Gemheart extends Item {
    public static final String NAME = "Gemheart";
    public static final int COST = 375;
    public static final Stats STATS = new Stats()
            .put(Stats.MAX_HP, 40);

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
