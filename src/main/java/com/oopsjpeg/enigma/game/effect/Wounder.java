package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.buff.Wound;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Wounder extends Effect {
    public Wounder(float power) {
        super("Wounder", power, null);
    }

    @Override
    public DamageEvent hitOut(DamageEvent event) {
        event.output.add(event.target.buff(new Wound(event.actor, 1, getPower())));
        return event;
    }

    @Override
    public String getDescription() {
        return "Reduces the target's healing and shielding by **" + Util.percent(getPower()) + "** on hit.";
    }
}
