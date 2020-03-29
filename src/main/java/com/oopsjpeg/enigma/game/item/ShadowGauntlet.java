package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.LoveOfWar;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class ShadowGauntlet extends Item {
    public static final String NAME = "Shadow Gauntlet";
    public static final Tree TREE = Tree.ABILITY;
    public static final String TIP = "stacking basic attack damage";
    public static final int COST = 1100;
    public static final Item[] BUILD = new Item[]{new MidnightDagger(), new Ring()};
    public static final Effect[] EFFECTS = new Effect[]{new LoveOfWar(0.15f)};
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 10)
            .put(Stats.ABILITY_POWER, 25);

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