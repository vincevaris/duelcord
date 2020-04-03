package com.oopsjpeg.enigma.util;

public class Stacker implements Notifier {
    private int max;
    private int current = 0;
    private boolean notified = false;

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

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getCurrent() {
        return this.current;
    }

    public void setCurrent(int current) {
        this.current = Util.limit(current, 0, max);
    }

    public boolean isNotified() {
        return this.notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
