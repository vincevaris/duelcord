package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.util.Emote;

public class Silence extends Buff {
    public Silence(GameMember source, int turns) {
        super(source, turns);
    }

    @Override
    public String getName() {
        return "Silence";
    }

    @Override
    public String onTurnStart(GameMember member) {
        return Emote.SILENCE + "**" + member.getUsername() + "** is silenced by **" + getSource().getUsername() + "** this turn.";
    }
}
