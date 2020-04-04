package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.GameObject;

public abstract class Buff extends GameObject {
    private final boolean debuff;
    private final GameMember source;
    private final float power;

    private int totalTurns;
    private int currentTurns;

    public Buff(String name, boolean debuff, GameMember source, int totalTurns, float power) {
        super(name);
        this.debuff = debuff;
        this.source = source;
        this.totalTurns = totalTurns;
        this.power = power;

        currentTurns = totalTurns;
    }

    public int turn() {
        currentTurns--;
        return currentTurns;
    }

    public boolean isDebuff() {
        return debuff;
    }

    public GameMember getSource() {
        return source;
    }

    public float getPower() {
        return power;
    }

    public boolean hasPower() {
        return power != 0;
    }

    public String formatPower() {
        return String.valueOf((int) Math.ceil(getPower()));
    }

    public int getTotalTurns() {
        return totalTurns;
    }

    public void setTotalTurns(int totalTurns) {
        this.totalTurns = totalTurns;
    }

    public int getCurrentTurns() {
        return currentTurns;
    }

    public void setCurrentTurns(int currentTurns) {
        this.currentTurns = currentTurns;
    }

    @Override
    public String toString() {
        return getName();
    }
}
