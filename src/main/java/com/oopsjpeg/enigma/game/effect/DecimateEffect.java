package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.buff.CrippleDebuff;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;

public class DecimateEffect extends Effect {
    private final Stacker critCount;

    public DecimateEffect(int critLimit, float power) {
        super("Decimate", power, null);
        this.critCount = new Stacker(critLimit);
    }

    @Override
    public DamageEvent critOut(DamageEvent event) {
        if (critCount.stack()) {
            event.output.add(event.target.buff(new CrippleDebuff(event.actor, 0, getPower())));
            critCount.reset();
        }
        return event;
    }

    @Override
    public String getDescription() {
        return "Every **" + critCount.getMax() + "** Crits, **Cripple** the target by __" + Util.percent(getPower()) + "__ until their next turn.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{
                "Decimate: **" + critCount.getCurrent() + " / " + critCount.getMax() + "**"};
    }
}
