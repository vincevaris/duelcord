package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

public class Weaken extends Buff {
    public Weaken(Game.Member source, int turns, float power) {
        super(source, turns, power);
    }

    @Override
    public String getName() {
        return "Weaken";
    }

    @Override
    public String onTurnStart(Game.Member member) {
        return Emote.SILENCE + "**" + member.getName() + "** is weakened by **" + Util.percent(getPower()) + "** from **"
                + getSource().getName() + "** this turn.";
    }
}
