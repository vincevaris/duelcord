package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.object.Item;

public class Gemheart extends Item {
    public Gemheart() {
        super("Gemheart", Tree.HEALTH, null, 375, null, null, new Stats()
                .put(Stats.MAX_HEALTH, 80));
    }
}
