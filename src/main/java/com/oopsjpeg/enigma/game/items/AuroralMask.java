package com.oopsjpeg.enigma.game.items;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effects.Vitalist;
import com.oopsjpeg.enigma.game.effects.util.Effect;
import com.oopsjpeg.enigma.game.items.util.Item;

public class AuroralMask extends Item {
    public static final String NAME = "Auroral Mask";
    public static final int COST = 600;
    public static final Item[] BUILD = new Item[]{new Crystal(), new Crystal()};
    public static final Effect[] EFFECTS = new Effect[]{new Vitalist(25)};
    public static final Stats STATS = new Stats();

    static {
        STATS.maxHp = 80;
    }

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
