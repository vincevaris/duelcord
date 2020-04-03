package com.oopsjpeg.enigma.util;

public class ChanceBag {
    private float chance;
    private float influence;
    private int attempts = 0;

    public ChanceBag(float chance) {
        this(chance, 1.0f);
    }

    public ChanceBag(float chance, float influence) {
        this.chance = chance;
        this.influence = influence;
    }

    public boolean get() {
        if (attempt() || (chance > 0 && Util.RANDOM.nextFloat() <= chance)) {
            attempts = 0;
            return true;
        }
        return false;
    }

    public boolean attempt() {
        if (chance > 0) {
            attempts++;
            return attempts >= 1 / (chance * influence);
        }
        return false;
    }

    public float getChance() {
        return this.chance;
    }

    public void setChance(float chance) {
        this.chance = chance;
    }

    public float getInfluence() {
        return this.influence;
    }

    public void setInfluence(float influence) {
        this.influence = influence;
    }

    public int getAttempts() {
        return this.attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
}
