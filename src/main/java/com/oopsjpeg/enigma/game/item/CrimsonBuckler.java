package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.Brawn;
import com.oopsjpeg.enigma.game.effect.Divinity;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class CrimsonBuckler extends Item {
    public static final String NAME = "Crimson Buckler";
    public static final Tree TREE = Tree.HEALTH;
    public static final String TIP = "Crit damage reduction.\nMore damage based on your max health.";
    public static final int COST = 1175;
    public static final Stats STATS = new Stats()
            .put(Stats.MAX_HEALTH, 200);
    public static final Effect[] EFFECTS = new Effect[]{new Divinity(0.2f), new Brawn(0.15f)};
    public static final Item[] BUILD = new Item[]{new DivinePlatemail(), new Gemheart()};

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
