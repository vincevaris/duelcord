package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.Surmount;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class ViktorsScythe extends Item {
    public static final String NAME = "Viktor's Scythe";
    public static final String TIP = "Strong damage against tanks.";
    public static final Tree TREE = Tree.DAMAGE;
    public static final int COST = 1425;
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 25)
            .put(Stats.MAX_HEALTH, 100);
    public static final Effect[] EFFECTS = new Effect[]{new Surmount(0.2f, 12)};
    public static final Item[] BUILD = new Item[]{new BlackHalberd(), new Hatchet()};

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Tree getTree() {
        return TREE;
    }

    @Override
    public String getTip() {
        return TIP;
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
