package com.oopsjpeg.enigma.game;

import java.util.ArrayList;
import java.util.List;

public class DamageEvent
{
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

    public boolean isAttack;
    public boolean isSkill;

    public float onHitScale;

    public DamageEvent(GameMember actor, GameMember target)
    {
        this.actor = actor;
        this.target = target;
    }

    public float total()
    {
        return damage + bonus;
    }
}