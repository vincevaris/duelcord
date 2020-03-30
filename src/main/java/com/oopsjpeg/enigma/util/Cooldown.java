package com.oopsjpeg.enigma.util;

import lombok.Getter;
import lombok.Setter;

public class Cooldown implements Notifier {
    @Getter @Setter private int duration;
    @Getter private int current = 0;
    @Getter @Setter private boolean notified = false;

    public Cooldown(int duration) {
        this.duration = duration;
    }

    public void start() {
        setCurrent(getDuration());
        setNotified(false);
    }

    public boolean count() {
        setCurrent(getCurrent() - 1);
        return isDone();
    }

    public boolean isDone() {
        return getCurrent() == 0;
    }

    public void setCurrent(int current) {
        this.current = Util.limit(current, 0, duration);
    }
}
