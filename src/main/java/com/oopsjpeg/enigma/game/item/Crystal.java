package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.object.Item;

public class Crystal extends Item {
    public Crystal() {
        super("Crystal", Tree.HEALTH, null, 200, null, null, new Stats()
                .put(Stats.MAX_HEALTH, 40));
    }
}
