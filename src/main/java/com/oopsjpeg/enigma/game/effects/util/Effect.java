package com.oopsjpeg.enigma.game.effects.util;

import com.oopsjpeg.enigma.game.Stats;

public abstract class Effect {
    public abstract String getName();

    public float getPower() {
        return 0;
    }

    public Stats getStats() {
        return new Stats();
    }

    public Stats getPerTurn() {
        return new Stats();
    }

    public void onTurn() {

    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(this.getClass());
    }
}
