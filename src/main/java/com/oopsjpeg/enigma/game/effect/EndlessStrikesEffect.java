package com.oopsjpeg.enigma.game.effect;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Effect;

import static com.oopsjpeg.enigma.util.Util.percent;

public class EndlessStrikesEffect extends Effect {
    private int multiplier = 0;

    public EndlessStrikesEffect(float power) {
        super("Endless Strikes", power, null);
    }

    @Override
    public String onTurnEnd(GameMember member) {
        multiplier = 0;
        return null;
    }

    @Override
    public DamageEvent hitOut(DamageEvent event) {
        event.damage *= 1 + (multiplier * getPower());
        multiplier++;
        return event;
    }

    @Override
    public String getDescription() {
        return "Each Hit deals __" + percent(getPower()) + "__ more than the last for this turn.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{
                "Endless Strikes: " + percent(multiplier * getPower())
        };
    }
}
