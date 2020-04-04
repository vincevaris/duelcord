package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.DawnShield;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class SteelMallet extends Item {
    public SteelMallet() {
        super("Steel Mallet", Tree.HEALTH, "Unused energy shields", 625,
                new Item[]{new Ring(), new Crystal()},
                new Effect[]{new DawnShield(50)},
                new Stats()
                        .put(Stats.ABILITY_POWER, 15)
                        .put(Stats.MAX_HEALTH, 60));
    }
}
