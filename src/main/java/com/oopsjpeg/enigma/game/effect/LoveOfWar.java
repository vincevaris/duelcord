package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class LoveOfWar extends Effect {
    public static final String NAME = "Love of War";
    private final float power;

    private int stack = 0;

    public LoveOfWar(float power) {
        this.power = power;
    }

    public int stack() {
        setStack(getStack() + 1);
        return stack;
    }

    public int getStack() {
        return stack;
    }

    public void setStack(int stack) {
        this.stack = stack;
    }

    @Override
    public String onTurnEnd(GameMember member) {
        setStack(0);
        return null;
    }

    @Override
    public DamageEvent hitOut(DamageEvent event) {
        event.damage *= 1 + ((stack() - 1) * power);
        return event;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Increases damage dealt by **" + Util.percent(power) + "** for the rest of the turn on hit.";
    }

    @Override
    public float getPower() {
        return power;
    }
}
