package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.KorasMight;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class KorasScepter extends Item {
    public static final String NAME = "Kora's Scepter";
    public static final String TIP = "More damage on abilities.";
    public static final Tree TREE = Tree.ABILITY;
    public static final int COST = 650;
    public static final Item[] BUILD = new Item[]{new Staff()};
    public static final Effect[] EFFECTS = new Effect[]{new KorasMight(3)};
    public static final Stats STATS = new Stats()
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
