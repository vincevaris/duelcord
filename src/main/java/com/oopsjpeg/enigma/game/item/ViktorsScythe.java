package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.Surmount;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class ViktorsScythe extends Item {
    public static final String NAME = "Viktor's Scythe";
    public static final int COST = 1275;
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 20);
    public static final Effect[] EFFECTS = new Effect[]{new Surmount(0.035f)};
    public static final Item[] BUILD = new Item[]{new BlackHalberd(), new Hatchet()};

    @Override
    public String getName() {
        return NAME;
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
