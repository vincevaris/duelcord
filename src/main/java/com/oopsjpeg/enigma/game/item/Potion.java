package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.buff.PotionHealing;
import com.oopsjpeg.enigma.game.obj.Item;

public class Potion extends Item {
    public static final int HEAL = 120;
    public static final int TURNS = 2;

    public static final String NAME = "Potion";
    public static final String DESC = "Heals for **" + HEAL + "** health over **" + TURNS + "** turns.";
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
        member.getData().add(new PotionHealing(member, TURNS));
        return member.heal(HEAL / TURNS, "Potion");
    }
}
