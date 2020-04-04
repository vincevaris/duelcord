package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.Divinity;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class DivinePlatemail extends Item {
    public DivinePlatemail() {
        super("Divine Platemail", Tree.HEALTH, "Less damage from abilities", 425,
                new Item[]{new Crystal()},
                new Effect[]{new Divinity(0.15f)},
                new Stats()
                        .put(Stats.MAX_HEALTH, 60)
                        .put(Stats.RESIST, 0.05f));
    }
}
