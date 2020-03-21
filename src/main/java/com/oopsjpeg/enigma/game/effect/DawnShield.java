package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Effect;

public class DawnShield extends Effect {
    private final float power;

    public DawnShield(float power) {
        this.power = power;
    }

    @Override
    public String onTurnEnd(Game.Member member) {
        if (member.getStats().get(Stats.ENERGY) > 0)
            return member.shield(Math.min(power, member.getStats().get(Stats.ENERGY)));
        return "";
    }

    @Override
    public String getName() {
        return "Dawn Shield";
    }

    @Override
    public String getDescription() {
        return "Unused energy after a turn shields for the same amount, up to **" + Math.round(power) + "**.";
    }

    @Override
    public float getPower() {
        return power;
    }
}
