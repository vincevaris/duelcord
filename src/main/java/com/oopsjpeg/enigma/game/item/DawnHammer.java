package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.DawnShield;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class DawnHammer extends Item {
    public DawnHammer() {
        super("Dawn Hammer", Tree.HEALTH, "25 more energy", 1225,
                new Item[]{new SteelMallet(), new Gemheart()},
                new Effect[]{new StatsEffect(new Stats().put(Stats.ENERGY_PER_TURN, 25)), new DawnShield(100)},
                new Stats()
                        .put(Stats.ABILITY_POWER, 25)
                        .put(Stats.MAX_HEALTH, 140));
    }
}
