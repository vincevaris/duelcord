package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.effect.StatsEffect;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;

public class IronScimitar extends Item {
    public IronScimitar() {
        super("Iron Scimitar", Tree.DAMAGE, "More crit damage", 1350,
                new Item[]{new BronzeCutlass(), new Hatchet()},
                new Effect[]{new StatsEffect(new Stats().put(Stats.CRIT_DAMAGE, 0.2f))},
                new Stats()
                        .put(Stats.DAMAGE, 25)
                        .put(Stats.CRIT_CHANCE, 0.5f));
    }
}