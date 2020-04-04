package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.Wolfbite;
import com.oopsjpeg.enigma.game.effect.Wounder;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class WolfsTooth extends Item {
    public WolfsTooth() {
        super("Wolf's Tooth", Tree.DAMAGE, "10% weaken", 1025,
                new Item[]{new BoneSpear(), new Knife()},
                new Effect[]{new Wounder(0.4f), new Wolfbite(0.1f, 3)},
                new Stats().put(Stats.DAMAGE, 15));
    }
}
