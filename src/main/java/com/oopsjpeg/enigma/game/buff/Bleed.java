package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.util.Emote;

public class Bleed extends Buff {
    public Bleed(GameMember source, int turns, float power) {
        super(source, true, turns, power);
    }

    @Override
    public String getName() {
        return "Bleed";
    }

    @Override
    public String onTurnStart(GameMember member) {
        DamageEvent event = new DamageEvent(member.getGame(), getSource(), member);
        event.damage = getPower();
        return getSource().damage(event, Emote.BLEED, "Bleed");
    }
}
