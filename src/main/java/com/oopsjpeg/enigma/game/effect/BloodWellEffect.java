package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Util;

import static com.oopsjpeg.enigma.util.Util.percent;

public class BloodWellEffect extends Effect {
    private final int maxShield;

    private int currentShield = 0;

    public BloodWellEffect(float power, int maxShield) {
        super("Blood Well", power, null);
        this.maxShield = maxShield;
    }

    public int getMaxShield() {
        return maxShield;
    }

    public int getCurrentShield() {
        return currentShield;
    }

    @Override
    public String onTurnStart(GameMember member) {
        currentShield = 0;
        return null;
    }

    @Override
    public DamageEvent attackOut(DamageEvent event) {
        float shieldAmount = event.damage + event.bonus;

        shieldAmount = Util.limit(shieldAmount, 0, maxShield - currentShield);

        event.shield = shieldAmount;

        return event;
    }

    @Override
    public String getDescription() {
        return "Attacks Shield for __" + percent(getPower()) + "__ of damage dealt.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{
                "Blood Well: " + currentShield + " / " + maxShield
        };
    }
}
