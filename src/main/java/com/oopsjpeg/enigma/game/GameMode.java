package com.oopsjpeg.enigma.game;

import java.util.Arrays;

public enum GameMode {
    DUEL("Duel", 2);
    //SKIRMISH("Skirmish (Beta)", 3);

    private final String name;
    private final int size;

    GameMode(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public static GameMode fromName(String name) {
        return Arrays.stream(values())
                .filter(g -> name.equalsIgnoreCase(g.getName()) || (name.length() >= 3
                        && g.getName().toLowerCase().startsWith(name.toLowerCase())))
                .findAny().orElse(null);
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}
