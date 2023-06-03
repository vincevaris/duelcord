package com.oopsjpeg.enigma.util;

/*

 */
public class Pity {
    private float chance;
    private float influence;

    private int rolls = 0;

    public Pity(float chance) {
        this(chance, 1.0f);
    }

    public Pity(float chance, float influence) {
        this.chance = chance;
        this.influence = influence;
    }

    public boolean roll() {
        return chance > 0 && (attempt() || Util.RANDOM.nextFloat() <= chance);
    }

    private boolean attempt() {
        rolls++;

        if (rolls >= 1 / (chance * influence)) {
            rolls = 0;
            return true;
        }

        return false;
    }

    public float getChance() {
        return chance;
    }

    public void setChance(float chance) {
        this.chance = chance;
    }

    public float getInfluence() {
        return influence;
    }

    public void setInfluence(float influence) {
        this.influence = influence;
    }

    public int getRolls() {
        return rolls;
    }

    public void setRolls(int rolls) {
        this.rolls = rolls;
    }
}
