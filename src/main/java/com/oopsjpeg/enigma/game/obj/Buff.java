package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.GameObject;

public abstract class Buff extends GameObject {
    private final GameMember source;
    private final boolean debuff;
    private int turns;
    private float power = 0;

    public Buff(GameMember source, boolean debuff, int turns) {
        this.source = source;
        this.debuff = debuff;
        this.turns = turns;
    }

    public Buff(GameMember source, boolean debuff, int turns, float power) {
        this.source = source;
        this.debuff = debuff;
        this.turns = turns;
        this.power = power;
    }

    public int turn() {
        turns--;
        return turns;
    }

    public abstract String getName();

    public boolean hasPower() {
        return power != 0;
    }

    public String getFormattedPower() {
        return String.valueOf(Math.round(getPower()));
    }

    @Override
    public String toString() {
        return getName();
    }

    public GameMember getSource() {
        return this.source;
    }

    public boolean isDebuff() {
        return this.debuff;
    }

    public int getTurns() {
        return this.turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public float getPower() {
        return this.power;
    }
}
