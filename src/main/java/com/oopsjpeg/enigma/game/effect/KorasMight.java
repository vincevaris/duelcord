package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class KorasMight extends Effect {
    public static final String NAME = "Kora's Might";
    private final float power;
    private final float ap;

    public KorasMight(float power) {
        this(power, 0);
    }

    public KorasMight(float power, float ap) {
        this.power = power;
        this.ap = ap;
    }

    @Override
    public DamageEvent onAbility(DamageEvent event) {
        event.bonus += power + (event.actor.getStats().get(Stats.ABILITY_POWER) * ap);
        return event;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDesc() {
        return "Abilities do **" + Math.round(power) + "** " + (ap > 0 ? "(+" + Util.percent(ap) + " AP) " : "") + "bonus damage.";
    }

    @Override
    public float getPower() {
        return power;
    }
}
