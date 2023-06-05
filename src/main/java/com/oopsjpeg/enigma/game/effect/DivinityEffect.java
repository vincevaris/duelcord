package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Effect;

import static com.oopsjpeg.enigma.game.Stats.SKILL_POWER;
import static com.oopsjpeg.enigma.util.Util.percent;

public class DivinityEffect extends Effect
{
    private final float spRatio;

    public DivinityEffect(float power)
    {
        this(power, 0);
    }

    public DivinityEffect(float power, float spRatio)
    {
        super("Divinity", power, null);
        this.spRatio = spRatio;
    }

    public float getTotalPower(float sp)
    {
        return getPower() + (sp * spRatio);
    }

    @Override
    public String onDefend(GameMember member)
    {
        return member.shield(getTotalPower(member.getStats().get(SKILL_POWER)));
    }

    @Override
    public String getDescription()
    {
        return "Defending Shields for **" + getPower() + "** " + (spRatio > 0 ? "+ __" + percent(spRatio) + " Skill Power__" : "") + ".";
    }
}
