package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;

public abstract class Effect extends GameObject {
    public abstract String getName();

    public abstract String getDesc();

    public float getPower() {
        return 0;
    }

    public Stats getStats() {
        return new Stats();
    }

    public Stats getPerTurn() {
        return new Stats();
    }

    @Override
    public String toString() {
        return getName();
    }
}
