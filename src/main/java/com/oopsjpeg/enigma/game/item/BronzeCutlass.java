package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.object.Item;

public class BronzeCutlass extends Item {
    public BronzeCutlass() {
        super("Bronze Cutlass", Tree.DAMAGE, null, 600, new Item[]{new Knife()}, null, new Stats()
                .put(Stats.DAMAGE, 8)
                .put(Stats.CRIT_CHANCE, 0.25f));
    }
}
