package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;

public abstract class Effect extends GameObject {
    public abstract String getName();

    public abstract String getDescription();

    public float getPower() {
        return 0;
    }

    public Stats getStats(Game.Member member) {
        return new Stats();
    }

    @Override
    public String toString() {
        return getName();
    }
}
