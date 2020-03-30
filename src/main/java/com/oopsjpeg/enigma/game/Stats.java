package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.util.Util;

import java.util.HashMap;
import java.util.Map;

public class Stats {
    public static final String HEALTH = "health";
    public static final String GOLD = "gold";
    public static final String ENERGY = "energy";
    public static final String SHIELD = "shield";

    public static final String MAX_HEALTH = "max_health";
    public static final String DAMAGE = "damage";
    public static final String ABILITY_POWER = "ability_power";
    public static final String CRIT_CHANCE = "crit_chance";
    public static final String CRIT_DAMAGE = "crit_damage";
    public static final String LIFE_STEAL = "life_steal";
    public static final String RESIST = "resist";

    public static final String GOLD_PER_TURN = "gold_per_turn";
    public static final String HEALTH_PER_TURN = "health_per_turn";
    public static final String ENERGY_PER_TURN = "energy_per_turn";

    private Map<String, Float> values = new HashMap<>();

    public Stats() {
        values.put(HEALTH, 0.0f);
        values.put(GOLD, 0.0f);
        values.put(ENERGY, 0.0f);
        values.put(SHIELD, 0.0f);

        values.put(MAX_HEALTH, 0.0f);
        values.put(DAMAGE, 0.0f);
        values.put(ABILITY_POWER, 0.0f);
        values.put(CRIT_CHANCE, 0.0f);
        values.put(CRIT_DAMAGE, 0.0f);
        values.put(LIFE_STEAL, 0.0f);
        values.put(RESIST, 0.0f);

        values.put(GOLD_PER_TURN, 0.0f);
        values.put(HEALTH_PER_TURN, 0.0f);
        values.put(ENERGY_PER_TURN, 0.0f);
    }

    public Stats put(String key, float value) {
        switch (key) {
            case MAX_HEALTH:
            case ENERGY:
            case SHIELD:
            case DAMAGE:
            case ABILITY_POWER:
            case GOLD_PER_TURN:
            case HEALTH_PER_TURN:
            case ENERGY_PER_TURN:
                value = Math.max(0, value);
                break;
            case HEALTH:
                value = Util.limit(value, 0, values.get(MAX_HEALTH));
                break;
            case CRIT_CHANCE:
                value = Util.limit(value, 0, 1);
                break;
            case RESIST:
                value = Util.limit(value, 0, 0.8f);
                break;
        }
        values.put(key, value);
        return this;
    }

    public float get(String key) {
        return values.getOrDefault(key, 0.0f);
    }

    public int getInt(String key) {
        return (int) Math.ceil(get(key));
    }

    public Stats add(String key, float value) {
        put(key, get(key) + value);
        return this;
    }

    public Stats sub(String key, float value) {
        put(key, get(key) - value);
        return this;
    }

    public Stats mul(String key, float value) {
        put(key, get(key) * value);
        return this;
    }

    public Stats div(String key, float value) {
        put(key, get(key) / value);
        return this;
    }

    public Stats putAll(Stats other) {
        other.values.keySet().forEach(k -> put(k, other.values.get(k)));
        return this;
    }

    public Stats addAll(Stats other) {
        other.values.keySet().forEach(k -> put(k, values.get(k) + other.values.get(k)));
        return this;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}