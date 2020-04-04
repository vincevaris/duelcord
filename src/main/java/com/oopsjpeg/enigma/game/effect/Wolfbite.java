package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.buff.Weaken;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Util;

public class Wolfbite extends Effect {
    private final int attacks;

    private int stack;

    public Wolfbite(float power, int attacks) {
        super("Wolfbite", power, null);
        this.attacks = attacks;
    }

    @Override
    public DamageEvent hitOut(DamageEvent event) {
        stack++;
        if (stack >= attacks) {
            event.output.add(event.target.buff(new Weaken(event.actor, 1, getPower())));
            stack = 0;
        }
        return event;
    }

    @Override
    public String getDescription() {
        return "Every **" + attacks + "** attacks, the enemy is weakened by **" + Util.percent(getPower()) + "** for **1** turn.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{"Wolfbite: **" + stack + " / " + attacks + "**"};
    }
}
