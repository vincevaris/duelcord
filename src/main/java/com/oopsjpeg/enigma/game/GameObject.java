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

    public DamageEvent onDamage(DamageEvent event) {
        return event;
    }

    public DamageEvent wasDamage(DamageEvent event) {
        return event;
    }

    public DamageEvent onBasicAttack(DamageEvent event) {
        return event;
    }

    public DamageEvent wasBasicAttack(DamageEvent event) {
        return event;
    }

    public DamageEvent onHit(DamageEvent event) {
        return event;
    }

    public DamageEvent wasHit(DamageEvent event) {
        return event;
    }

    public DamageEvent onCrit(DamageEvent event) {
        return event;
    }

    public DamageEvent wasCrit(DamageEvent event) {
        return event;
    }

    public DamageEvent onAbility(DamageEvent event) {
        return event;
    }

    public DamageEvent wasAbility(DamageEvent event) {
        return event;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(this.getClass());
    }
}
