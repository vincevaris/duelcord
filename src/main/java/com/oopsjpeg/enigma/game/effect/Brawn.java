package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Brawn extends Effect {
    public Brawn(float power) {
        super("Brawn", power, null);
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        event.damage += event.actor.getBonusHealth() * getPower();
        return event;
    }

    @Override
    public String getDescription() {
        return "Basic attacks do bonus damage equal to **" + Util.percent(getPower()) + "** of bonus max health.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{"Brawn: **" + Math.round(member.getBonusHealth() * getPower()) + "**"};
    }
}
