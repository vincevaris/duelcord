package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Util;

public class StatsEffect extends Effect {
    public StatsEffect(Stats stats) {
        super("Stats", 0, stats);
    }

    @Override
    public String getDescription() {
        return Util.formatStats(getStats());
    }
}
