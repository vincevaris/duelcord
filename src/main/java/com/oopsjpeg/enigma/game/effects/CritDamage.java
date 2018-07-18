package com.oopsjpeg.enigma.game.effects;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.effects.util.Effect;

public class CritDamage extends Effect {
    public static final String NAME = "Critical Damage";
    private final Stats stats = new Stats();
    private final int level;

    public CritDamage(int level) {
        this.level = level;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Stats getStats() {
        stats.critDamage = 0.1f * level;
        return stats;
    }

    @Override
    public int getLevel() {
        return level;
    }
}
