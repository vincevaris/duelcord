package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Util;

public class LoveOfWar extends Effect {
    private int stack = 0;

    public LoveOfWar(float power) {
        super("Love of War", power, null);
    }

    public int stack() {
        stack++;
        return stack;
    }

    @Override
    public String onTurnEnd(GameMember member) {
        stack = 0;
        return null;
    }

    @Override
    public DamageEvent hitOut(DamageEvent event) {
        event.damage *= 1 + ((stack() - 1) * getPower());
        return event;
    }

    @Override
    public String getDescription() {
        return "Increases damage dealt by **" + Util.percent(getPower()) + "** for the rest of the turn on hit.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{"Love of War: **" + Util.percent(stack * getPower()) + "**"};
    }
}
