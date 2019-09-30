package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Surmount extends Effect {
    public static final String NAME = "Surmount";
    private final float power;

    private boolean firstAtk = true;

    public Surmount(float power) {
        this.power = power;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDesc() {
        return "The first basic attack in a turn deals **" + Util.percent(power) + "** of the target's max health as bonus damage.";
    }

    @Override
    public float getPower() {
        return power;
    }

    @Override
    public String onTurnEnd(Game.Member member) {
        firstAtk = true;
        return "";
    }

    @Override
    public DamageEvent onHit(DamageEvent event) {
        if(firstAtk){
            event.damage *= power * event.target.getStats().get("max_hp");
            firstAtk = false;
        }
        return event;
    }
}
