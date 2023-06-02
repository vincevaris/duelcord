package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.buff.WoundDebuff;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;

public class LifewasterEffect extends Effect {
    private final Stacker hitCount;

    public LifewasterEffect(int hitLimit, float power) {
        super("Lifewaster", power, null);
        hitCount = new Stacker(hitLimit);
    }

    @Override
    public DamageEvent hitOut(DamageEvent event) {
        if (hitCount.stack()) {
            event.output.add(event.target.buff(new WoundDebuff(event.actor, 1, getPower())));
            hitCount.reset();
        }
        return event;
    }

    @Override
    public String getDescription() {
        return "Every " + hitCount.getMax() + " Hits, **Wound** the target by __" + Util.percent(getPower()) + "__ on their next turn.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{
                "Lifewaster: " + hitCount.getCurrent() + " / " + hitCount.getMax()
        };
    }
}
