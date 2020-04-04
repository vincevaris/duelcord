package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.KorasMight;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class KorasScepter extends Item {
    public KorasScepter() {
        super("Kora's Scepter", Tree.ABILITY, "More ability damage", 675,
                new Item[]{new Staff()},
                new Effect[]{new KorasMight(6)},
                new Stats().put(Stats.ABILITY_POWER, 30));
    }
}
