package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.object.Item;

public class Staff extends Item {
    public Staff() {
        super("Staff", Tree.ABILITY, null, 400, null, null, new Stats()
                .put(Stats.ABILITY_POWER, 30));
    }
}
