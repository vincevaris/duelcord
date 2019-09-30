package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;

import java.awt.*;

public class Crusader extends Unit {
    public static final String NAME = "Crusader";
    public static final String DESC = "";
    public static final Color COLOR = Color.ORANGE;
    public static final Stats STATS = new Stats()
            .put(Stats.ENERGY, 125)
            .put(Stats.MAX_HP, 750)
            .put(Stats.DAMAGE, 17);
    public static final Stats PER_TURN = new Stats()
            .put(Stats.HP, 11);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDesc() {
        return DESC;
    }

    @Override
    public Color getColor() {
        return COLOR;
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
