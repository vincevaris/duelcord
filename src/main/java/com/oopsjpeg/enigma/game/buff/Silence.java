package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;

public class Silence extends Buff {
    public Silence(GameMember source, int totalTurns) {
        super("Silence", true, source, totalTurns, 0);
    }

    @Override
    public String onTurnStart(GameMember member) {
        return Emote.SILENCE + "**" + member.getUsername() + "** is silenced by **" + getSource().getUsername() + "** this turn.";
    }
}
