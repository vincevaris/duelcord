package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;

public class BloodlustBlade extends Item {
    public static final String NAME = "Bloodlust Blade";
    public static final Tree TREE = Tree.DAMAGE;
    public static final int COST = 550;
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 10)
            .put(Stats.LIFE_STEAL, 0.15f);
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
