package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.DawnShield;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class DawnHammer extends Item {
    public static final String NAME = "Dawn Hammer";
    public static final Tree TREE = Tree.HEALTH;
    public static final String TIP = "shields + 25 energy";
    public static final int COST = 1225;
    public static final Item[] BUILD = new Item[]{new SteelMallet(), new Gemheart()};
    public static final Effect[] EFFECTS = new Effect[]{
            new StatsEffect(new Stats().put(Stats.ENERGY_PER_TURN, 25)),
            new DawnShield(100)};
    public static final Stats STATS = new Stats()
            .put(Stats.ABILITY_POWER, 25)
            .put(Stats.MAX_HEALTH, 140);

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
