package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;

public class Staff extends Item {
    public static final String NAME = "Staff";
    public static final Tree TREE = Tree.ABILITY;
    public static final int COST = 400;
    public static final Stats STATS = new Stats()
            .put(Stats.ABILITY_POWER, 30);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Tree getTree() {
        return TREE;
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
