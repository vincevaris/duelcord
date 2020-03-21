package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Starlight extends Effect {
    public static final String NAME = "Starlight";
    private final float power;

    public Starlight(float power) {
        this.power = power;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Ability power is increased by **" + Util.percent(power) + "**.";
    }

    @Override
    public float getPower() {
        return power;
    }

    @Override
    public Stats getStats(Game.Member member) {
        return new Stats().add(Stats.ABILITY_POWER, member.getStats().get(Stats.ABILITY_POWER) * power);
    }
}
