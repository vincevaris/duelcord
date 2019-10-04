package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.Divinity;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class DivinePlatemail extends Item {
    public static final String NAME = "Divine Platemail";
    public static final int COST = 475;
    public static final Stats STATS = new Stats()
            .put(Stats.MAX_HP, 60);
    public static final Effect[] EFFECTS = new Effect[]{new Divinity(0.15f)};
    public static final Item[] BUILD = new Item[]{new Crystal()};

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
