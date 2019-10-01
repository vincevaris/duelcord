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
    public String getName() {
        return NAME;
    }

    @Override
    public String getDesc() {
        return "Basic attacks deal bonus damage equal to **" + Util.percent(power) + "** of max health.";
    }

    @Override
    public float getPower() {
        return power;
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
        event.damage += event.actor.getStats().get(Stats.MAX_HP) * power;
        return event;
    }
}
