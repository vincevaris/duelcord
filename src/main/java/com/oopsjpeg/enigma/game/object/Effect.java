package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;

public abstract class Effect extends GameObject {
    private final float power;
    private final Stats stats;

    public Effect(String name, float power, Stats stats) {
        super(name);
        this.power = power;
        this.stats = stats;
    }

    public float getPower() {
        return power;
    }

    public Stats getStats() {
        return stats != null ? stats : new Stats();
    }

    @Override
    public String toString() {
        return getName();
    }
}
