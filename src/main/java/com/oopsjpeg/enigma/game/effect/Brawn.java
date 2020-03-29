package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Brawn extends Effect {
    public static final String NAME = "Brawn";
    private final float power;

    public Brawn(float power) {
        this.power = power;
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        event.damage += (event.actor.getStats().get(Stats.MAX_HEALTH) - event.actor.getUnit().getStats().get(Stats.MAX_HEALTH)) * power;
        return event;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Basic attacks do more damage equal to **" + Util.percent(power) + "** of bonus max health.";
    }

    @Override
    public float getPower() {
        return power;
    }
}
