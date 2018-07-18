package com.oopsjpeg.enigma.game.effects;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effects.util.Effect;

public class Vampirism extends Effect {
    public static final String NAME = "Vampirism";
    private final Stats stats = new Stats();
    private final int level;

    public Vampirism(int level) {
        this.level = level;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Stats getStats() {
        stats.lifeSteal = 0.1f * level;
        return stats;
    }

    @Override
    public int getLevel() {
        return level;
    }
}
