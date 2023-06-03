package com.oopsjpeg.enigma.game;

import java.util.ArrayList;
import java.util.List;

public class DamageEvent {
    public final Game game;
    public final List<String> output = new ArrayList<>();
    public GameMember actor;
    public GameMember target;
    public float damage;
    public float bonus;
    public boolean crit;
    public float critMul;
    public float heal;
    public float shield;
    public boolean cancelled;

    public DamageEvent(Game game, GameMember actor, GameMember target) {
        this.game = game;
        this.actor = actor;
        this.target = target;
    }

    public float total() {
        return damage + bonus;
    }
}