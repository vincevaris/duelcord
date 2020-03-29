package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Surmount extends Effect {
    public static final String NAME = "Surmount";
    private final float power;
    private final float flat;

    private boolean bonus = false;

    public Surmount(float power, float flat) {
        this.power = power;
        this.flat = flat;
    }

    @Override
    public String onTurnEnd(Game.Member member) {
        bonus = false;
        return "";
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        if (!bonus) {
            event.bonus += flat + ((event.target.getStats().get(Stats.MAX_HEALTH) - event.target.getUnit().getStats().get(Stats.MAX_HEALTH)) * power);
            bonus = true;
        }
        return event;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "The first basic attack per turn deals bonus damage equal to **" + flat + "** + **" + Util.percent(power) + "** of the target's bonus max health.";
    }

    @Override
    public float getPower() {
        return power;
    }

    public float getFlat() {
        return flat;
    }
}
