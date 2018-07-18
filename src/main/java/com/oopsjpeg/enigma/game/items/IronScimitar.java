package com.oopsjpeg.enigma.game.items;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effects.Bruiser;
import com.oopsjpeg.enigma.game.effects.CritDamage;
import com.oopsjpeg.enigma.game.effects.util.Effect;
import com.oopsjpeg.enigma.game.items.util.Item;

public class IronScimitar extends Item {
    public static final String NAME = "Iron Scimitar";
    public static final int COST = 1200;
    public static final Item[] BUILD = new Item[]{new BronzeCutlass(), new Hatchet()};
    public static final Effect[] EFFECTS = new Effect[]{new CritDamage(5), new Bruiser(4)};
    public static final Stats STATS = new Stats();

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