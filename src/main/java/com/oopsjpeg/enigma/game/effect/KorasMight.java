package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Util;

public class KorasMight extends Effect {
    private final float apRatio;

    public KorasMight(float power) {
        this(power, 0);
    }

    public KorasMight(float power, float apRatio) {
        super("Kora's Might", power, null);
        this.apRatio = apRatio;
    }

    private float get(float ap) {
        return getPower() + (ap * apRatio);
    }

    @Override
    public DamageEvent abilityOut(DamageEvent event) {
        event.bonus += get(event.actor.getStats().get(Stats.ABILITY_POWER));
        return event;
    }

    @Override
    public String getDescription() {
        return "Abilities do **" + Math.round(getPower()) + "** " + (apRatio > 0 ? "(+" + Util.percent(apRatio) + " AP) " : "") + "more damage.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{"Kora's Might: **" + get(member.getStats().get(Stats.ABILITY_POWER)) + "**"};
    }
}
