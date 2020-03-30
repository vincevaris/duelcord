package com.oopsjpeg.enigma.util;

import lombok.Getter;
import lombok.Setter;

public class Stacker implements Notifier {
    @Getter @Setter private int max;
    @Getter private int current = 0;
    @Getter @Setter private boolean notified = false;

    public Stacker(int max) {
        this.max = max;
    }

    public boolean stack() {
        setCurrent(getCurrent() + 1);
        return isDone();
    }

    public void reset() {
        setCurrent(0);
        setNotified(false);
    }

    public boolean isDone() {
        return getCurrent() == getMax();
    }

    public void setCurrent(int current) {
        this.current = Util.limit(current, 0, max);
    }
}
