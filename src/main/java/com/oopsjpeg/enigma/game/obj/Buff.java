package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.GameObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public abstract class Buff extends GameObject {
    @Getter private final GameMember source;
    @Getter private final boolean debuff;
    @Getter @Setter private int turns;
    @Getter private float power = 0;

    public Buff(GameMember source, boolean debuff, int turns) {
        this.source = source;
        this.debuff = debuff;
        this.turns = turns;
    }

    public int turn() {
        turns--;
        return turns;
    }

    public abstract String getName();

    @Override
    public String toString() {
        return getName();
    }
}
