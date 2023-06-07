package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;

import static com.oopsjpeg.enigma.game.Stats.MAX_ENERGY;

public class AwakenedFaithBuff extends Buff
{
    public AwakenedFaithBuff(GameMember source, float power)
    {
        super("Awakened Faith", false, source, 2, power);
    }

    @Override
    public String getStatus(GameMember member)
    {
        return "Awakened Faith: " + Math.round(getPower()) + " bonus Energy this turn";
    }

    @Override
    public String onTurnStart(GameMember member)
    {
        return Emote.ENERGY + "**" + member.getUsername() + "** gained __" + formatPower() + "__ bonus Energy this turn.";
    }

    @Override
    public Stats getStats()
    {
        return new Stats()
                .put(MAX_ENERGY, getPower());
    }
}
