package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;

public class DebuffBleed extends Buff {
    public DebuffBleed(GameMember source, int totalTurns, float power) {
        super("Bleed", true, source, totalTurns, power);
    }

    @Override
    public String onTurnStart(GameMember member) {
        DamageEvent event = new DamageEvent(member.getGame(), getSource(), member);
        event.damage = getPower();
        return getSource().damage(event, Emote.BLEED, "Bleed");
    }
}
