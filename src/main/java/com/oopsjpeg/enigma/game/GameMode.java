package com.oopsjpeg.enigma.game;

public enum GameMode {
    DUEL("Duel", 2);

    private final String name;
    private final int players;

    GameMode(String name, int players) {
        this.name = name;
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public int getPlayers() {
        return players;
    }
}
