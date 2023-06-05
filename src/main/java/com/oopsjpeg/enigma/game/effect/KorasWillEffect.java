package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Effect;

import static com.oopsjpeg.enigma.game.Stats.SKILL_POWER;
import static com.oopsjpeg.enigma.util.Util.percent;

public class KorasWillEffect extends Effect
{
    private final float spRatio;

    public KorasWillEffect(float power)
    {
        this(power, 0);
    }

    public KorasWillEffect(float power, float spRatio)
    {
        super("Kora's Will", power, null);
        this.spRatio = spRatio;
    }

    private float getTotalPower(float sp)
    {
        return getPower() + (sp * spRatio);
    }

    @Override
    public DamageEvent abilityOut(DamageEvent event)
    {
        event.bonus += getTotalPower(event.actor.getStats().get(SKILL_POWER));
        return event;
    }

    @Override
    public String getDescription()
    {
        return "Skills do **" + Math.round(getPower()) + "** " + (spRatio > 0 ? "+ __" + percent(spRatio) + " Skill Power__ " : "") + "more damage.";
    }

    @Override
    public String[] getTopic(GameMember member)
    {
        return new String[]{
                "Kora's Will: " + getTotalPower(member.getStats().get(SKILL_POWER))
        };
    }
}
