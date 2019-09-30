package com.oopsjpeg.enigma.util;

public class Stacker implements Notifier {
    private final int max;
    private int cur = 0;
    private boolean notif = false;

    public Stacker(int max) {
        this.max = max;
    }

    public boolean stack() {
        setCur(getCur() + 1);
        return done();
    }

    public void reset() {
        setCur(0);
        setNotif(false);
    }

    public boolean done() {
        return getCur() == getMax();
    }

    public int getMax() {
        return max;
    }

    public int getCur() {
        return cur;
    }

    public void setCur(int cur) {
        this.cur = Util.limit(cur, 0, max);
    }

    @Override
    public boolean getNotif() {
        return notif;
    }

    @Override
    public void setNotif(boolean notif) {
        this.notif = notif;
    }
}
