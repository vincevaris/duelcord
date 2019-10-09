package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.DawnShield;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class DawnHammer extends Item {
    public static final String NAME = "Dawn Hammer";
    public static final Tree TREE = Tree.HEALTH;
    public static final String TIP = "Unused energy shields you.\nMore energy.";
    public static final int COST = 1275;
    public static final Item[] BUILD = new Item[]{new SteelMallet(), new Gemheart()};
    public static final Effect[] EFFECTS = new Effect[]{StatsEffect.perTurn(new Stats()
            .put(Stats.ENERGY, 25)), new DawnShield(100)};
    public static final Stats STATS = new Stats()
            .put(Stats.DAMAGE, 10)
            .put(Stats.MAX_HEALTH, 180);

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
