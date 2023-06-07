package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.buff.WoundedDebuff;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;

import static com.oopsjpeg.enigma.util.Util.percent;

public class LifewasterEffect extends Effect
{
    private final Stacker hitCount;

    public LifewasterEffect(int hitLimit, float power)
    {
        super("Lifewaster", power, null);
        hitCount = new Stacker(hitLimit);
    }

    @Override
    public DamageEvent hitOut(DamageEvent event)
    {
        if (hitCount.stack())
        {
            event.output.add(event.target.addBuff(new WoundedDebuff(event.actor, 1, getPower()), Emote.WOUND));
            hitCount.reset();
        }
        return event;
    }

    @Override
    public String getDescription()
    {
        return "Every " + hitCount.getMax() + " Hits, **Wound** the target by __" + percent(getPower()) + "__ on their next turn.";
    }

    @Override
    public String getStatus(GameMember member)
    {
        return "Lifewaster: " + hitCount.getCurrent() + "/" + hitCount.getMax() + " (" + percent(getPower()) + ")";
    }
}
