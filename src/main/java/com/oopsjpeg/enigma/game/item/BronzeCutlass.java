package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;

public class BronzeCutlass extends Item {
    public static final String NAME = "Bronze Cutlass";
    public static final Tree TREE = Tree.DAMAGE;
    public static final int COST = 600;
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 8)
            .put(Stats.CRIT_CHANCE, 0.25f);
    public static final Item[] BUILD = new Item[]{new Knife()};

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
    public Item[] getBuild() {
        return BUILD;
    }

    @Override
    public Stats getStats() {
        return STATS;
    }
}
