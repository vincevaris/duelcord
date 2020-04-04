package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.LoveOfWar;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class ShadowGauntlet extends Item {
    public ShadowGauntlet() {
        super("Shadow Gauntlet", Tree.ABILITY, "Stacking b2b damage", 1100,
                new Item[]{new MidnightDagger(), new Ring()},
                new Effect[]{new LoveOfWar(0.15f)},
                new Stats()
                        .put(Stats.DAMAGE, 10)
                        .put(Stats.ABILITY_POWER, 25));
    }
}