package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.obj.Buff;

public class Silence extends Buff {
    public Silence(Game.Member source, int turns) {
        super(source, turns);
    }

    @Override
    public String getName() {
        return "Silence";
    }
}
