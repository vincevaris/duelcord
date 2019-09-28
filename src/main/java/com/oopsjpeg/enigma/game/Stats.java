package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.util.Util;

import java.util.HashMap;
import java.util.Map;

public class Stats {
	public static String HP = "hp";
	public static String GOLD = "gold";
	public static String ENERGY = "energy";
	public static String SHIELD = "shield";

	public static String MAX_HP = "max_hp";
	public static String DAMAGE = "damage";
	public static String ABILITY_POWER = "ability_power";
	public static String CRIT_CHANCE = "crit_chance";
	public static String CRIT_DAMAGE = "crit_damage";
	public static String LIFE_STEAL = "life_steal";

	private Map<String, Float> values = new HashMap<>();

	public Stats() {
		values.put(HP, 0.0f);
		values.put(GOLD, 0.0f);
		values.put(ENERGY, 0.0f);
		values.put(SHIELD, 0.0f);

		values.put(MAX_HP, 0.0f);
		values.put(DAMAGE, 0.0f);
		values.put(ABILITY_POWER, 0.0f);
		values.put(CRIT_CHANCE, 0.0f);
		values.put(CRIT_DAMAGE, 0.0f);
		values.put(LIFE_STEAL, 0.0f);
	}

	public Stats(Stats other) {
		for (String key : values.keySet())
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
		for (String key : values.keySet())
			values.put(key, values.get(key) + other.values.get(key));
		return this;
	}

	public Stats sub(Stats other) {
		for (String key : values.keySet())
			values.put(key, values.get(key) - other.values.get(key));
		return this;
	}

	public Stats clean() {
		values.put(MAX_HP, Math.max(0, values.get(MAX_HP)));

		values.put(HP, Util.limit(values.get(HP), 0, values.get(MAX_HP)));
		values.put(ENERGY, Math.max(0, values.get(ENERGY)));
		values.put(SHIELD, Math.max(0, values.get(SHIELD)));

		values.put(DAMAGE, Math.max(0, values.get(DAMAGE)));
		values.put(ABILITY_POWER, Math.max(0, values.get(ABILITY_POWER)));
		values.put(CRIT_CHANCE, Util.limit(values.get(CRIT_CHANCE), 0, 1));

		return this;
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
