package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;

public class SilenceDebuff extends Buff {
    public SilenceDebuff(GameMember source) {
        super("Silence", true, source, 1, 0);
    }

    @Override
    public String onTurnStart(GameMember member) {
        return Emote.SILENCE + "They can't use skills or defend.";
    }
}
