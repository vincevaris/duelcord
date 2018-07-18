package com.oopsjpeg.enigma.game.effects;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effects.util.Effect;

public class Vitalist extends Effect {
    public static final String NAME = "Vitalist";
    private final float power;
    private final Stats perTurn = new Stats();

    public Vitalist(float power) {
        this.power = power;
    }

    @Override
    public float getPower() {
        return power;
    }

    @Override
    public Stats getPerTurn() {
        perTurn.energy = (int) power;
        return perTurn;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
