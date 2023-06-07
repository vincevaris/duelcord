package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.buff.CrippledDebuff;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;

import static com.oopsjpeg.enigma.util.Util.percent;

public class DecimateEffect extends Effect
{
    private final Stacker critCount;

    public DecimateEffect(int critLimit, float power)
    {
        super("Decimate", power, null);
        this.critCount = new Stacker(critLimit);
    }

    @Override
    public DamageEvent critOut(DamageEvent event)
    {
        if (critCount.stack())
        {
            event.output.add(event.target.addBuff(new CrippledDebuff(event.actor, 1, getPower()), Emote.CRIPPLE));
            critCount.reset();
        }
        return event;
    }

    @Override
    public String getDescription()
    {
        return "Every **" + critCount.getMax() + "** Crits, **Cripple** the target by __" + percent(getPower()) + "__ until their next turn.";
    }

    @Override
    public String getStatus(GameMember member)
    {
        return "Decimate: " + critCount.getCurrent() + "/" + critCount.getMax() + " (" + percent(getPower()) + ")";
    }
}
