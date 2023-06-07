package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;

import static com.oopsjpeg.enigma.util.Util.percent;

public class WeakenedDebuff extends Buff
{
    public WeakenedDebuff(GameMember source, int totalTurns, float power)
    {
        super("Weakened", true, source, totalTurns, power);
    }

    @Override
    public String getStatus(GameMember member)
    {
        return "Weakened: " + percent(getPower()) + " (" + getCurrentTurns() + " turns left)";
    }

    @Override
    public String onTurnStart(GameMember member)
    {
        return Emote.WEAKEN + "**" + member.getUsername() + "** deals __" + formatPower() + "__ less damage this turn.";
    }

    @Override
    public String formatPower()
    {
        return percent(getPower());
    }

    @Override
    public DamageEvent damageOut(DamageEvent event)
    {
        event.damage *= 1 - getPower();
        event.bonus *= 1 - getPower();
        return event;
    }
}
