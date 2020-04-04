package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.object.Item;

public class Ring extends Item {
    public Ring() {
        super("Ring", Tree.ABILITY, null, 225, null, null, new Stats()
                .put(Stats.ABILITY_POWER, 15));
    }
}
