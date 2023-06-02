package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;

import static com.oopsjpeg.enigma.game.Stats.MAX_ENERGY;

public class RestingFaithBuff extends Buff {
    public RestingFaithBuff(GameMember source, float power) {
        super("Resting Faith", false, source, 1, power);
    }

    @Override
    public String onTurnStart(GameMember member) {
        return Emote.ENERGY + "**" + member.getUsername() + "** gained **" + formatPower() + "** bonus energy this turn.";
    }

    @Override
    public String getName() {
        return "Resting Faith";
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(MAX_ENERGY, getPower());
    }
}
