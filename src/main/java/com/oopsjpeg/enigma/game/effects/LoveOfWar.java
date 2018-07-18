package com.oopsjpeg.enigma.game.effects;

import com.oopsjpeg.enigma.game.effects.util.Effect;

public class LoveOfWar extends Effect {
    public static final String NAME = "Love of War";
    private final int level;

    private int attack = 0;

    public LoveOfWar(int level) {
        this.level = level;
    }

    public int attack() {
        attack++;
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    @Override
    public void onTurn() {
        attack = 0;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getLevel() {
        return level;
    }
}
