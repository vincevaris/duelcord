package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.Wounder;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class WolfsTooth extends Item {
    public static final String NAME = "Wolf's Tooth";
    public static final Tree TREE = Tree.DAMAGE;
    public static final String TIP = "Powerful anti-sustain.";
    public static final int COST = 1025;
    public static final Item[] BUILD = new Item[]{new BoneSpear(), new Knife()};
    public static final Effect[] EFFECTS = new Effect[]{new Wounder(0.4f)};
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 15);

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
