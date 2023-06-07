package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;

import static com.oopsjpeg.enigma.util.Util.percent;

public class CrippledDebuff extends Buff
{
    public CrippledDebuff(GameMember source, int totalTurns, float power)
    {
        super("Crippled", true, source, totalTurns, power);
    }

    @Override
    public String getStatus(GameMember member)
    {
        return "Crippled: Taking " + percent(getPower()) + " more damage";
    }

    @Override
    public String formatPower()
    {
        return percent(getPower());
    }

    @Override
    public DamageEvent damageIn(DamageEvent event)
    {
        event.damage *= 1 + getPower();
        event.bonus *= 1 + getPower();
        return event;
    }
}
