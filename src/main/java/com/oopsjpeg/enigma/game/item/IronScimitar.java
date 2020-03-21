package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class IronScimitar extends Item {
    public static final String NAME = "Iron Scimitar";
    public static final Tree TREE = Tree.DAMAGE;
    public static final String TIP = "More damage on crits.";
    public static final int COST = 1350;
    public static final Item[] BUILD = new Item[]{new BronzeCutlass(), new Hatchet()};
    public static final Effect[] EFFECTS = new Effect[]{
            new StatsEffect(new Stats().put(Stats.CRIT_DAMAGE, 0.2f))};
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 25)
            .put(Stats.CRIT_CHANCE, 0.5f);

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