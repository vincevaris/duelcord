package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class Soulstealer extends Item {
    public Soulstealer() {
        super("Soulstealer", Tree.DAMAGE, "25% life steal", 1375,
                new Item[]{new BloodlustBlade(), new Hatchet()},
                new Effect[]{new StatsEffect(new Stats().put(Stats.LIFE_STEAL, 0.25f))},
                new Stats().put(Stats.DAMAGE, 22));
    }
}
