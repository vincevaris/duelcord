package com.oopsjpeg.enigma.game;

public enum GameMode {
    DUEL("Duel", 2);

    private final String name;
    private final int size;

    GameMode(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}
