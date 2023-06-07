package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;

import static com.oopsjpeg.enigma.util.Util.percent;

public class BleedingDebuff extends Buff
{
    public BleedingDebuff(GameMember source, int totalTurns, float power)
    {
        super("Bleeding", true, source, totalTurns, power);
    }

    @Override
    public String getStatus(GameMember member)
    {
        return "Bleeding: Taking " + Math.round(getPower()) + " damage (" + getCurrentTurns() + " turns left)";
    }

    @Override
    public String onTurnStart(GameMember member)
    {
        DamageEvent event = new DamageEvent(getSource(), member);
        event.damage = getPower();
        return member.damage(event, Emote.BLEED, "Bleeding");
    }
}
