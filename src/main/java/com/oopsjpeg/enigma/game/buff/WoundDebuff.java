package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;

import static com.oopsjpeg.enigma.util.Util.percent;

public class WoundDebuff extends Buff {
    public WoundDebuff(GameMember source, int totalTurns, float power) {
        super("Wound", true, source, totalTurns, power);
    }

    @Override
    public String onTurnStart(GameMember member) {
        return Emote.WOUND + "They receive __" + percent(getPower()) + "__ less healing.";
    }

    @Override
    public float onHeal(float healAmount) {
        return healAmount * (1 - getPower());
    }

    @Override
    public String formatPower() {
        return percent(getPower());
    }
}
