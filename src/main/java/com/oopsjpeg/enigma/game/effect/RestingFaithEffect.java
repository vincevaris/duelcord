package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.buff.AwakenedFaithBuff;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Emote;

public class RestingFaithEffect extends Effect
{
    public RestingFaithEffect(float power)
    {
        super("Resting Faith", power, null);
    }

    @Override
    public String onDefend(GameMember member)
    {
        if (!member.hasBuff(AwakenedFaithBuff.class))
            return member.addBuff(new AwakenedFaithBuff(member, getPower()), Emote.ENERGY);
        return null;
    }

    @Override
    public String getDescription()
    {
        return "Defending grants **" + Math.round(getPower()) + "** bonus energy on the next turn." +
                "\nCan't be activated while already in use.";
    }
}
