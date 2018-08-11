package com.oopsjpeg.enigma.game;

public class Stats {
	public int hp = 0;
	public int gold = 0;
	public int energy = 0;
	public int shield = 0;

	public int maxHp = 1;
	public int damage = 1;
	public float accuracy = 1.0f;
	public float critChance = 0.0f;
	public float critDamage = 0.0f;
	public float lifeSteal = 0.0f;

	public Stats() { }

	public Stats(Stats other) {
		hp = other.hp;
		gold = other.gold;
		energy = other.energy;
		shield = other.shield;

		maxHp = other.maxHp;
		damage = other.damage;
		accuracy = other.accuracy;
		critChance = other.critChance;
		critDamage = other.critDamage;
		lifeSteal = other.lifeSteal;
	}

	public Stats add(Stats other) {
		hp += other.hp;
		gold += other.gold;
		energy += other.energy;
		shield += other.shield;

		maxHp += other.maxHp;
		damage += other.damage;
		accuracy += other.accuracy;
		critChance += other.critChance;
		lifeSteal += other.lifeSteal;

		return clean();
	}

	public Stats subtract(Stats other) {
		hp -= other.hp;
		gold -= other.gold;
		energy -= other.energy;
		shield -= other.shield;

		maxHp -= other.maxHp;
		damage -= other.damage;
		accuracy -= other.accuracy;
		critChance -= other.critChance;
		lifeSteal -= other.lifeSteal;

		return clean();
	}

	public Stats clean() {
		gold = Math.max(0, gold);
		energy = Math.max(0, energy);
		shield = Math.max(0, shield);

		maxHp = Math.max(1, maxHp);
		damage = Math.max(1, damage);
		accuracy = Math.max(0, Math.min(1, accuracy));
		critChance = Math.max(0, Math.min(1, critChance));
		lifeSteal = Math.max(0, Math.min(1, lifeSteal));

		return this;
	}
}
