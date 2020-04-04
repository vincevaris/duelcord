package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.object.Item;

public class Hatchet extends Item {
    public Hatchet() {
        super("Hatchet", Tree.DAMAGE, null, 450, null, null, new Stats()
                .put(Stats.DAMAGE, 10));
    }
}
