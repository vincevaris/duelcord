package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.util.Stacker;

import static com.oopsjpeg.enigma.game.Stats.COOLDOWN_REDUCTION;
import static com.oopsjpeg.enigma.game.Stats.MAX_HEALTH;
import static com.oopsjpeg.enigma.util.Util.percent;

public class MagicalMasteryEffect extends Effect {
    private final int cdReduction;
    private final Stacker skillCount;

    public MagicalMasteryEffect(int cdReduction, int skillLimit, float power) {
        super("Magical Mastery", power, null);
        this.cdReduction = cdReduction;
        this.skillCount = new Stacker(skillLimit);
    }

    @Override
    public DamageEvent abilityOut(DamageEvent event) {
        if (skillCount.stack()) {
            event.bonus += event.target.getStats().get(MAX_HEALTH) * getPower();
            skillCount.reset();
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "Skills recharge **" + cdReduction + "** turns faster." +
                "\nEvery **" + skillCount.getMax() + "** damaging Skills, deal __" + percent(getPower()) + "__ of the target's max health.";
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(COOLDOWN_REDUCTION, cdReduction);
    }
}
