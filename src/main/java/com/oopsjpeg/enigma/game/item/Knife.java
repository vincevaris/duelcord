package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.object.Item;

public class Knife extends Item {
    public Knife() {
        super("Knife", Tree.DAMAGE, null, 250, null, null, new Stats()
                .put(Stats.DAMAGE, 5));
    }
}
