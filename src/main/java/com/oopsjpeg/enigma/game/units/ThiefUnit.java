package com.oopsjpeg.enigma.game.units;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.units.util.Unit;

public class ThiefUnit extends Unit {
    public static final String NAME = "Thief";
    public static final Stats STATS = new Stats();
    public static final Stats PER_TURN = new Stats();

    static {
        STATS.energy = 150;
        STATS.maxHp = 494;
        STATS.damage = 19;
        STATS.critChance = 0.15f;
        PER_TURN.hp = 12;
        PER_TURN.gold = 50;
    }

    private boolean crit = false;

    public boolean getCrit() {
        return crit;
    }

    public void setCrit(boolean crit) {
        this.crit = crit;
    }

    @Override
    public String onTurn() {
        crit = false;
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
