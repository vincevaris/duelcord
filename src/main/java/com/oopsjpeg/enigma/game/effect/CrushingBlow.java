package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.buff.Weaken;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class CrushingBlow extends Effect {
    private final float power;
    private final int turns;

    private int turn;

    public CrushingBlow(float power, int turns) {
        this.power = power;
        this.turns = turns;
    }

    @Override
    public DamageEvent hitOut(DamageEvent event) {
        turn++;
        if (turn >= turns) {
            event.output.add(event.target.buff(new Weaken(event.actor, 1, power)));
            turn = 0;
        }
        return event;
    }

    @Override
    public String getName() {
        return "Crushing Blow";
    }

    @Override
    public String getDescription() {
        return "Every **" + turns + "** attacks weakens the enemy by **" + Util.percent(power) + "** for **1** turn.";
    }

    @Override
    public float getPower() {
        return power;
    }
}
