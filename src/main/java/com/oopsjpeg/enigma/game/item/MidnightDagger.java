package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.LoveOfWar;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class MidnightDagger extends Item {
    public MidnightDagger() {
        super("Midnight Dagger", Tree.DAMAGE, "Stacking b2b damage", 450,
                new Item[]{new Knife()},
                new Effect[]{new LoveOfWar(0.08f)},
                new Stats().put(Stats.DAMAGE, 5));
    }
}
