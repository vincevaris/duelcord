package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;

public class Gemheart extends Item {
    public static final String NAME = "Gemheart";
    public static final Tree TREE = Tree.HEALTH;
    public static final int COST = 375;
    public static final Stats STATS = new Stats()
            .put(Stats.MAX_HEALTH, 80);

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
