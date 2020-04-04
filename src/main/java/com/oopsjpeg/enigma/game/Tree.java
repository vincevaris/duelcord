package com.oopsjpeg.enigma.game;

import java.awt.*;

public enum Tree {
    CONSUMABLES("Consumables", new Color(168, 232, 255)),
    DAMAGE("Damage", new Color(255, 168, 168)),
    HEALTH("Health", new Color(201, 255, 168)),
    ABILITY("Ability", new Color(239, 168, 255));

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