package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.util.Util;

import java.util.HashMap;
import java.util.Map;

public class Stats {
    public static String HEALTH = "health";
    public static String GOLD = "gold";
    public static String ENERGY = "energy";
    public static String SHIELD = "shield";

    public static String MAX_HEALTH = "max_health";
    public static String DAMAGE = "damage";
    public static String ABILITY_POWER = "ability_power";
    public static String CRIT_CHANCE = "crit_chance";
    public static String CRIT_DAMAGE = "crit_damage";
    public static String LIFE_STEAL = "life_steal";
    public static String RESIST = "resist";

    public static String GOLD_PER_TURN = "gold_per_turn";
    public static String HEALTH_PER_TURN = "health_per_turn";
    public static String ENERGY_PER_TURN = "energy_per_turn";

    private Map<String, Float> values = new HashMap<>();

    public Stats() {
        put(HEALTH, 0.0f);
        put(GOLD, 0.0f);
        put(ENERGY, 0.0f);
        put(SHIELD, 0.0f);

        put(MAX_HEALTH, 0.0f);
        put(DAMAGE, 0.0f);
        put(ABILITY_POWER, 0.0f);
        put(CRIT_CHANCE, 0.0f);
        put(CRIT_DAMAGE, 0.0f);
        put(LIFE_STEAL, 0.0f);
        put(RESIST, 0.0f);

        put(GOLD_PER_TURN, 0.0f);
        put(HEALTH_PER_TURN, 0.0f);
        put(ENERGY_PER_TURN, 0.0f);
    }

    public Stats(Stats other) {
        for (String key : other.values.keySet())
            values.put(key, other.values.get(key));
    }

    public Stats put(String key, float value) {
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
        return clean();
    }

    public Stats sub(String key, float value) {
        put(key, get(key) - value);
        return clean();
    }

    public Stats mul(String key, float value) {
        put(key, get(key) * value);
        return clean();
    }

    public Stats div(String key, float value) {
        put(key, get(key) / value);
        return clean();
    }

    public Stats add(Stats other) {
        for (String key : other.values.keySet())
            values.put(key, values.get(key) + other.values.get(key));
        return this;
    }

    public Stats sub(Stats other) {
        for (String key : other.values.keySet())
            values.put(key, values.get(key) - other.values.get(key));
        return this;
    }

    public Stats clean() {
        put(MAX_HEALTH, Math.max(0, values.get(MAX_HEALTH)));

        put(HEALTH, Util.limit(values.get(HEALTH), 0, values.get(MAX_HEALTH)));
        put(ENERGY, Math.max(0, values.get(ENERGY)));
        put(SHIELD, Math.max(0, values.get(SHIELD)));

        put(DAMAGE, Math.max(0, values.get(DAMAGE)));
        put(ABILITY_POWER, Math.max(0, values.get(ABILITY_POWER)));
        put(CRIT_CHANCE, Util.limit(values.get(CRIT_CHANCE), 0, 1));
        put(RESIST, Util.limit(values.get(RESIST), 0, 0.8f));

        put(GOLD_PER_TURN, Math.max(values.get(GOLD_PER_TURN), 0));
        put(HEALTH_PER_TURN, Math.max(values.get(HEALTH_PER_TURN), 0));
        put(ENERGY_PER_TURN, Math.max(values.get(ENERGY_PER_TURN), 0));

        return this;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}