package com.oopsjpeg.enigma.game.units;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.units.util.Unit;

public class BerserkerUnit extends Unit {
    public static final String NAME = "Berserker";
    public static final Stats STATS = new Stats();
    public static final Stats PER_TURN = new Stats();

    static {
        STATS.energy = 75;
        STATS.maxHp = 513;
        STATS.damage = 28;
        PER_TURN.hp = 13;
        PER_TURN.gold = 50;
    }

    private int rage = 0;

    public int getRage() {
        return rage;
    }

    public void setRage(int rage) {
        this.rage = Math.min(6, rage);
    }

    public int rage() {
        setRage(rage + 1);
        return rage;
    }

    @Override
    public String onDefend() {
        rage();
        return "";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Stats getStats() {
        return STATS;
    }

    @Override
    public Stats getPerTurn() {
        return PER_TURN;
    }
}
