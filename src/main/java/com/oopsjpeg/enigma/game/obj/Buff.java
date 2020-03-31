package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.GameObject;

public abstract class Buff extends GameObject {
    private final GameMember source;
    private int turns;
    private float power = 0;

    public Buff(GameMember source, int turns) {
        this.source = source;
        this.turns = turns;
    }

    public Buff(GameMember source, int turns, float power) {
        this(source, turns);
        this.power = power;
    }

    public GameMember getSource() {
        return source;
    }

    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public int turn() {
        turns--;
        return turns;
    }

    public abstract String getName();

    public float getPower() {
        return power;
    }

    @Override
    public String toString() {
        return getName();
    }
}
