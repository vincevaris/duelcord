package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

public class Wound extends Buff {
    public Wound(GameMember source, int totalTurns, float power) {
        super("Wound", true, source, totalTurns, power);
    }

    @Override
    public String onTurnStart(GameMember member) {
        return Emote.WOUND + "**" + member.getUsername() + "** is wounded by **" + Util.percent(getPower()) + "** by **" + getSource().getUsername() + "** this turn.";
    }

    @Override
    public String formatPower() {
        return Util.percent(getPower());
    }
}
