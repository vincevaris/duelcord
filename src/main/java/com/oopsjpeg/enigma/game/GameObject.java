package com.oopsjpeg.enigma.game;

public abstract class GameObject {
    public String onTurnStart(Game.Member member) {
        return "";
    }

    public String onTurnEnd(Game.Member member) {
        return "";
    }

    public String onDefend(Game.Member member) {
        return "";
    }

    public DamageEvent damageOut(DamageEvent event) {
        return event;
    }

    public DamageEvent damageIn(DamageEvent event) {
        return event;
    }

    public DamageEvent basicAttackOut(DamageEvent event) {
        return event;
    }

    public DamageEvent basicAttackIn(DamageEvent event) {
        return event;
    }

    public DamageEvent hitOut(DamageEvent event) {
        return event;
    }

    public DamageEvent hitIn(DamageEvent event) {
        return event;
    }

    public DamageEvent critOut(DamageEvent event) {
        return event;
    }

    public DamageEvent critIn(DamageEvent event) {
        return event;
    }

    public DamageEvent abilityOut(DamageEvent event) {
        return event;
    }

    public DamageEvent abilityIn(DamageEvent event) {
        return event;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(this.getClass());
    }
}
