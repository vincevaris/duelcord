package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Divinity extends Effect {
    public static final String NAME = "Divinity";
    private final float power;

    public Divinity(float power) {
        this.power = power;
    }

    @Override
    public DamageEvent abilityIn(DamageEvent event) {
        event.damage *= 1 - power;
        return event;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Reduces damage taken from abilities by **" + Util.percent(power) + "**.";
    }

    @Override
    public float getPower() {
        return power;
    }
}
