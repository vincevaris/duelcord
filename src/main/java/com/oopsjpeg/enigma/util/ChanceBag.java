package com.oopsjpeg.enigma.util;

import lombok.Getter;
import lombok.Setter;

public class ChanceBag {
    @Getter @Setter private float chance;
    @Getter @Setter private float influence;
    @Getter @Setter private int attempts = 0;

    public ChanceBag(float chance) {
        this(chance, 1.0f);
    }

    public ChanceBag(float chance, float influence) {
        this.chance = chance;
        this.influence = influence;
    }

    public boolean get() {
        if (attempt() || (chance > 0 && Util.RANDOM.nextFloat() <= chance)) {
            attempts = 0;
            return true;
        }
        return false;
    }

    public boolean attempt() {
        if (chance > 0) {
            attempts++;
            return attempts >= 1 / (chance * influence);
        }
        return false;
    }
}
