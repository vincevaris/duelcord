package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.KorasMight;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class Shatterwand extends Item {
    public Shatterwand() {
        super("Shatterwand", Tree.ABILITY, "More ability damage", 1275,
                new Item[]{new KorasScepter(), new Ring()},
                new Effect[]{new KorasMight(10, 0.2f)},
                new Stats().put(Stats.ABILITY_POWER, 50));
    }
}
