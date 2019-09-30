package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.buff.PotionHealing;
import com.oopsjpeg.enigma.game.obj.Item;

public class Potion extends Item {
    public static final String NAME = "Potion";
    public static final String DESC = "Heals for **160** health over **2** turns.";
    public static final int COST = 50;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDesc() {
        return DESC;
    }

    @Override
    public int getCost() {
        return COST;
    }

    @Override
    public boolean canUse(Game.Member member) {
        return !member.hasData(PotionHealing.class);
    }

    @Override
    public boolean removeOnUse() {
        return true;
    }

    @Override
    public String onUse(Game.Member member) {
        member.getData().add(new PotionHealing(member, 2));
        return member.heal(80, "Potion");
    }
}
