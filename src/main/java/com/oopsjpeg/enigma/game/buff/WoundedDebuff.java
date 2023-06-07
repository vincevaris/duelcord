package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;

import static com.oopsjpeg.enigma.util.Util.percent;

public class WoundedDebuff extends Buff
{
    public WoundedDebuff(GameMember source, int totalTurns, float power)
    {
        super("Wounded", true, source, totalTurns, power);
    }

    @Override
    public String getStatus(GameMember member)
    {
        return "Wounded: " + percent(getPower()) + " (" + getCurrentTurns() + " turns left)";
    }

    @Override
    public String onTurnStart(GameMember member)
    {
        return Emote.WOUND + "**" + member.getUsername() + "** receives __" + formatPower() + "__ less healing this turn.";
    }

    @Override
    public float onHeal(float healAmount)
    {
        return healAmount * (1 - getPower());
    }

    @Override
    public String formatPower()
    {
        return percent(getPower());
    }
}
