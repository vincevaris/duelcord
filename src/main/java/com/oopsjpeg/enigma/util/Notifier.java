package com.oopsjpeg.enigma.util;

public interface Notifier {
    boolean isNotified();

    void setNotified(boolean notified);

    default boolean tryNotify() {
        if (isNotified())
            return false;
        setNotified(true);
        return true;
    }
}
