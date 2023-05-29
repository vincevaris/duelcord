package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

public class DebuffWound extends Buff {
    public DebuffWound(GameMember source, int totalTurns, float power) {
        super("Wound", true, source, totalTurns, power);
    }

    @Override
    public String onTurnStart(GameMember member) {
        return Emote.WOUND + "**" + member.getUsername() + "** is wounded for **" + Util.percent(getPower()) + "** by **" + getSource().getUsername() + "**.";
    }

    @Override
    public String formatPower() {
        return Util.percent(getPower());
    }
}
