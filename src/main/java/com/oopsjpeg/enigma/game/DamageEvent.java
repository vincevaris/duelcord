package com.oopsjpeg.enigma.game;

import java.util.ArrayList;
import java.util.List;

public class DamageEvent {
    public final Game game;
    public final Game.Member actor;
    public final Game.Member target;
    public List<String> output = new ArrayList<>();
    public float damage;
    public float bonus;
    public boolean crit;
    public float critMul;
    public boolean miss;

    public DamageEvent(Game game, Game.Member actor, Game.Member target) {
        this.game = game;
        this.actor = actor;
        this.target = target;
    }

    public float total() {
        return damage + bonus;
    }
}