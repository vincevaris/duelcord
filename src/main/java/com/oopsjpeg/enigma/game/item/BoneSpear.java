package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.Wounder;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class BoneSpear extends Item {
    public BoneSpear() {
        super("Bone Spear", Tree.DAMAGE, "Anti-healing", 375,
                new Item[]{new Knife()},
                new Effect[]{new Wounder(0.25f)},
                new Stats().put(Stats.DAMAGE, 5));
    }
}
