package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.DawnShield;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class SteelMallet extends Item {
    public static final String NAME = "Steel Mallet";
    public static final int COST = 575;
    public static final Item[] BUILD = new Item[]{new Knife(), new Crystal()};
    public static final Effect[] EFFECTS = new Effect[]{new DawnShield()};
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 6)
            .put(Stats.MAX_HP, 25);

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
