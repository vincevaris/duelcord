package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.KorasMight;
import com.oopsjpeg.enigma.game.effect.Starlight;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class StarlitBlaster extends Item {
    public static final String NAME = "Starlit Blaster";
    public static final Tree TREE = Tree.ABILITY;
    public static final String TIP = "Massive ability power.";
    public static final int COST = 1375;
    public static final Item[] BUILD = new Item[]{new Staff(), new Staff()};
    public static final Effect[] EFFECTS = new Effect[]{new Starlight(0.3f)};
    public static final Stats STATS = new Stats()
            .put(Stats.ABILITY_POWER, 60);

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
