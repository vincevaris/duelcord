package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.item.Potion;
import com.oopsjpeg.enigma.game.obj.Buff;

public class PotionHealing extends Buff {
    public PotionHealing(GameMember source, int turns) {
        super(source, false, turns);
    }

    @Override
    public String onTurnStart(GameMember member) {
        return member.heal(Potion.HEAL / Potion.TURNS, "Potion");
    }

    @Override
    public String getName() {
        return "Potion Healing";
    }
}
