package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;

public class Ring extends Item {
    public static final String NAME = "Ring";
    public static final Tree TREE = Tree.ABILITY;
    public static final int COST = 225;
    public static final Stats STATS = new Stats()
            .put(Stats.ABILITY_POWER, 15);

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
