package com.oopsjpeg.enigma.game;

public interface GameObject {
    String getName();

    default String getDescription() {
        return null;
    }

    default String[] getTopic(GameMember member) {
        return null;
    }

    default String onTurnStart(GameMember member) {
        return null;
    }

    default String onTurnEnd(GameMember member) {
        return null;
    }

    default String onDefend(GameMember member) {
        return null;
    }

    default float onHeal(float healAmount)
    {
        return healAmount;
    }

    default float onShield(float shieldAmount)
    {
        return shieldAmount;
    }

    default DamageEvent damageOut(DamageEvent event) {
        return event;
    }

    default DamageEvent damageIn(DamageEvent event) {
        return event;
    }

    default DamageEvent attackOut(DamageEvent event) {
        return event;
    }

    default DamageEvent attackIn(DamageEvent event) {
        return event;
    }

    default DamageEvent hitOut(DamageEvent event) {
        return event;
    }

    default DamageEvent hitIn(DamageEvent event) {
        return event;
    }

    default DamageEvent critOut(DamageEvent event) {
        return event;
    }

    default DamageEvent critIn(DamageEvent event) {
        return event;
    }

    default DamageEvent abilityOut(DamageEvent event) {
        return event;
    }

    default DamageEvent abilityIn(DamageEvent event) {
        return event;
    }

    default DamageEvent dodgeYou(DamageEvent event) {
        return event;
    }

    default DamageEvent dodgeMe(DamageEvent event) {
        return event;
    }
}
