package com.oopsjpeg.enigma.util;

public class Cooldown implements Notifier {
    private final int duration;
    private int cur = 0;
    private boolean notif = false;

    public Cooldown(int duration) {
        this.duration = duration;
    }

    public void start() {
        setCur(getDuration());
        setNotif(false);
    }

    public boolean count() {
        setCur(getCur() - 1);
        return done();
    }

    public boolean done() {
        return getCur() == 0;
    }

    public int getDuration() {
        return duration;
    }

    public int getCur() {
        return cur;
    }

    public void setCur(int cur) {
        this.cur = Util.limit(cur, 0, duration);
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
