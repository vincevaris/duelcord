package com.oopsjpeg.enigma.game;

import discord4j.rest.util.Color;

public enum Tree {
    BASIC("Basic Items", Color.CYAN),
    ADVANCED("Advanced Items", Color.ORANGE),
    COMPLETE("Complete Items", Color.MAGENTA);

    private final String name;
    private final Color color;

    Tree(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}