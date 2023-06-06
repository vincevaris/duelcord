package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;

public abstract class Effect implements GameObject
{
    private final String name;
    private final float power;
    private final Stats stats;

    public Effect(String name, float power, Stats stats)
    {
        this.name = name;
        this.power = power;
        this.stats = stats;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public float getPower()
    {
        return power;
    }

    @Override
    public String[] getTopic(GameMember member)
    {
        return new String[] {name + ": " + getPower()};
    }

    public Stats getStats()
    {
        return stats != null ? stats : new Stats();
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
