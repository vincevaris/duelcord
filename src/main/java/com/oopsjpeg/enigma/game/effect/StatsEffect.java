package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.util.Util;

public class StatsEffect extends Effect {
    private final Stats stats;

    public StatsEffect(Stats stats) {
        this.stats = stats;
    }

    @Override
    public String getName() {
        return "Stats";
    }

    @Override
    public String getDescription() {
        return Util.formatStats(stats);
    }

    @Override
    public Stats getStats(GameMember member) {
        return stats;
    }
}
