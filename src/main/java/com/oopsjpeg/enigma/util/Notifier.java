package com.oopsjpeg.enigma.util;

public interface Notifier {
    boolean getNotif();

    void setNotif(boolean notif);

    default boolean notif() {
        if (getNotif()) return false;
        setNotif(true);
        return true;
    }
}
