package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.util.Emote;

public class Silence extends Buff {
    public Silence(Game.Member source, int turns) {
        super(source, turns);
    }

    @Override
    public String getName() {
        return "Silence";
    }

    @Override
    public String onTurnStart(Game.Member member) {
        return Emote.SILENCE + "**" + member.getName() + "** is silenced by **" + getSource().getName() + "** this turn.";
    }
}
