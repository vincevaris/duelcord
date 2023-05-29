package com.oopsjpeg.enigma.game;

import discord4j.rest.util.Color;

public enum Tree {
    CONSUMABLES("Consumables", Color.of(168, 232, 255)),
    DAMAGE("Damage", Color.of(255, 168, 168)),
    HEALTH("Health", Color.of(201, 255, 168)),
    ABILITY("Ability", Color.of(239, 168, 255));

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