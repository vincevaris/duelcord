package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;

public class BuffPotion extends Buff {
    public BuffPotion(GameMember source, int totalTurns, float power) {
        super("Potion", false, source, totalTurns, power);
    }

    @Override
    public String onTurnStart(GameMember member) {
        return member.heal(getPower() / getTotalTurns(), "Potion");
    }

    @Override
    public String getName() {
        return "Potion";
    }
}
