package com.oopsjpeg.enigma.game;

public class Stats {
    public int hp;
    public int gold;
    public int energy;

    public int maxHp;
    public int damage;
    public float critChance;
    public float critDamage;
    public float lifeSteal;

    public Stats() {
    }

    public Stats(Stats other) {
        hp = other.hp;
        gold = other.gold;
        energy = other.energy;

        maxHp = other.maxHp;
        damage = other.damage;
        critChance = other.critChance;
        critDamage = other.critDamage;
        lifeSteal = other.lifeSteal;
    }

    public Stats add(Stats other) {
        hp += other.hp;
        gold += other.gold;
        energy += other.energy;

        maxHp += other.maxHp;
        damage += other.damage;
        critChance += other.critChance;
        lifeSteal += other.lifeSteal;

        return clean();
    }

    public Stats subtract(Stats other) {
        hp -= other.hp;
        gold -= other.gold;
        energy -= other.energy;

        maxHp -= other.maxHp;
        damage -= other.damage;
        critChance -= other.critChance;
        lifeSteal -= other.lifeSteal;

        return clean();
    }

    public Stats clean() {
        energy = Math.max(0, energy);

        maxHp = Math.max(1, maxHp);
        damage = Math.max(1, damage);
        critChance = Math.max(0, Math.min(1, critChance));
        lifeSteal = Math.max(0, Math.min(1, lifeSteal));

        return this;
    }
}
