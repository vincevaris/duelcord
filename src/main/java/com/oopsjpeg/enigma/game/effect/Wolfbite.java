package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.buff.Weaken;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Wolfbite extends Effect {
    private final float power;
    private final int attacks;

    private int stack;

    public Wolfbite(float power, int attacks) {
        this.power = power;
        this.attacks = attacks;
    }

    @Override
    public DamageEvent hitOut(DamageEvent event) {
        stack++;
        if (stack >= attacks) {
            event.output.add(event.target.buff(new Weaken(event.actor, 1, power)));
            stack = 0;
        }
        return event;
    }

    @Override
    public String getName() {
        return "Wolfbite";
    }

    @Override
    public String getDescription() {
        return "Every **" + attacks + "** attacks, the enemy is weakened by **" + Util.percent(power) + "** for **1** turn.";
    }

    @Override
    public String[] getTopic() {
        return new String[]{"Wolfbite: **" + stack + " / " + attacks + "**"};
    }

    @Override
    public float getPower() {
        return power;
    }
}
