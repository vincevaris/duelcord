package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effect.CrushingBlow;
import com.oopsjpeg.enigma.game.effect.Wounder;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;

public class WolfsTooth extends Item {
    @Override
    public String getName() {
        return "Wolf's Tooth";
    }

    @Override
    public Tree getTree() {
        return Tree.DAMAGE;
    }

    @Override
    public String getTip() {
        return "anti-healing + weaken";
    }

    @Override
    public int getCost() {
        return 1025;
    }

    @Override
    public Item[] getBuild() {
        return new Item[]{new BoneSpear(), new Knife()};
    }

    @Override
    public Effect[] getEffects() {
        return new Effect[]{new Wounder(0.4f), new CrushingBlow(0.1f, 3)};
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.DAMAGE, 15);
    }
}
