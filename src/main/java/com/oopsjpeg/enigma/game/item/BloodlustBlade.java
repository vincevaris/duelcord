package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.object.Item;

public class BloodlustBlade extends Item {
    public BloodlustBlade() {
        super("Bloodlust Blade", Tree.DAMAGE, null, 575, new Item[]{new Knife()}, null, new Stats()
                .put(Stats.DAMAGE, 5)
                .put(Stats.LIFE_STEAL, 0.10f));
    }
}
