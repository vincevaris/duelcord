package com.oopsjpeg.enigma.util;

public class Cooldown implements Notifier {
    private int duration;
    private int current = 0;
    private boolean notified = false;

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

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCurrent() {
        return this.current;
    }

    public void setCurrent(int current) {
        this.current = Util.limit(current, 0, duration);
    }

    public boolean isNotified() {
        return this.notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
