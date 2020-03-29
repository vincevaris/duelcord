package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.LoveOfWar;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class MidnightDagger extends Item {
    public static final String NAME = "Midnight Dagger";
    public static final String TIP = "stacking basic attack damage";
    public static final Tree TREE = Tree.DAMAGE;
    public static final int COST = 450;
    public static final Item[] BUILD = new Item[]{new Knife()};
    public static final Effect[] EFFECTS = new Effect[]{new LoveOfWar(0.08f)};
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 5);

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
