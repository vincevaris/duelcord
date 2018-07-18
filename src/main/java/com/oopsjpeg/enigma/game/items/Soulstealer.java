package com.oopsjpeg.enigma.game.items;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effects.Vampirism;
import com.oopsjpeg.enigma.game.effects.util.Effect;
import com.oopsjpeg.enigma.game.items.util.Item;

public class Soulstealer extends Item {
    public static final String NAME = "Soulstealer";
    public static final int COST = 1275;
    public static final Item[] BUILD = new Item[]{new BloodlustBlade(), new Hatchet()};
    public static final Effect[] EFFECTS = new Effect[]{new Vampirism(0.2f)};
    public static final Stats STATS = new Stats();

    static {
        STATS.damage = 25;
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
