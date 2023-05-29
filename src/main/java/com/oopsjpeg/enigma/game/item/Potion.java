package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.buff.BuffPotion;
import com.oopsjpeg.enigma.game.object.Item;

public class Potion extends Item {
    public static final float HEAL = 120;
    public static final int TURNS = 2;

    public Potion() {
        super("Potion", Tree.CONSUMABLES, "Heals for **" + Math.round(HEAL) + "**", 50, null, null, null, false);
    }

    @Override
    public String onUse(GameMember member) {
        member.getData().add(new BuffPotion(member, TURNS, HEAL));
        return member.heal(HEAL / TURNS, "Potion");
    }

    @Override
    public boolean canUse(GameMember member) {
        return !member.hasData(BuffPotion.class);
    }

    @Override
    public boolean removeOnUse() {
        return true;
    }
}
