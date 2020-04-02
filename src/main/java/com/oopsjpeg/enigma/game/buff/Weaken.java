package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

public class Weaken extends Buff {
    public Weaken(GameMember source, int turns, float power) {
        super(source, true, turns, power);
    }

    @Override
    public String getName() {
        return "Weaken";
    }

    @Override
    public String onTurnStart(GameMember member) {
        return Emote.SILENCE + "**" + member.getUsername() + "** is weakened by **" + Util.percent(getPower()) + "** by **"
                + getSource().getUsername() + "** this turn.";
    }

    @Override
    public String getFormattedPower() {
        return Util.percent(getPower());
    }

    @Override
    public DamageEvent damageOut(DamageEvent event) {
        event.damage *= 1 - (getPower() / 2);
        event.bonus *= 1 - (getPower() / 2);
        return event;
    }

    @Override
    public DamageEvent damageIn(DamageEvent event) {
        event.damage *= 1 + getPower();
        event.bonus *= 1 + getPower();
        return event;
    }
}
