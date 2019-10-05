package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.KorasMight;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class Shatterwand extends Item {
    public static final String NAME = "Shatterwand";
    public static final Tree TREE = Tree.ABILITY;
    public static final String TIP = "More damage on abilities.";
    public static final int COST = 1325;
    public static final Item[] BUILD = new Item[]{new KorasScepter(), new Ring()};
    public static final Effect[] EFFECTS = new Effect[]{new KorasMight(5, 0.2f)};
    public static final Stats STATS = new Stats()
            .put(Stats.ABILITY_POWER, 50);

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
