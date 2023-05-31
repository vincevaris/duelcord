package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.object.Effect;

public class DawnShield extends Effect {
    public DawnShield(float power) {
        super("Dawn Shield", power, null);
    }

    @Override
    public String onDefend(GameMember member) {
        return member.shield(Math.min(getPower(), member.getEnergy()));
    }

    @Override
    public String getDescription() {
        return "Unused energy after a turn shields for the same amount, up to **" + Math.round(getPower()) + "**.";
    }
}
