package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Divinity extends Effect {
    public Divinity(float power) {
        super("Divinity", power, null);
    }

    @Override
    public DamageEvent abilityIn(DamageEvent event) {
        event.damage *= 1 - getPower();
        return event;
    }

    @Override
    public String getDescription() {
        return "Reduces damage taken from abilities by **" + Util.percent(getPower()) + "**.";
    }
}
