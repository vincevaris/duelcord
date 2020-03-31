package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

public class Wound extends Buff {
    public Wound(GameMember source, int turns, float power) {
        super(source, true, turns, power);
    }

    @Override
    public String getName() {
        return "Wound";
    }

    @Override
    public String onTurnStart(GameMember member) {
        return Emote.WOUND + "**" + member.getUsername() + "** is wounded by **" + Util.percent(getPower()) + "** by **"
                + getSource().getUsername() + "** this turn.";
    }
}
