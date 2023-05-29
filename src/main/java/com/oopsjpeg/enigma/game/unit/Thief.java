package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.object.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import discord4j.rest.util.Color;

public class Thief extends Unit {
    public static final float STEAL_AD = 0.35f;
    public static final float CRIT_REDUCE = 0.2f;
    public static final float CRIT_INCREASE = 0.15f;
    public static final int GOLD_TARGET = 125;
    public static final int BONUS_ENERGY = 50;

    private int crits = 0;
    private int goldStolen = 0;
    private boolean goldTargetHit = false;

    public Thief() {
        super("Thief", null, Color.YELLOW, null);
    }

    public int crit() {
        crits++;
        return crits;
    }

    @Override
    public String getDescription() {
        return "The first crit per turn steals gold equal to **" + Util.percent(STEAL_AD) + "** AD."
                + "\nAfter stealing **" + GOLD_TARGET + "** gold, **" + BONUS_ENERGY + "** energy is gained."
                + "\nCrit damage is reduced by **" + Util.percent(CRIT_REDUCE) + "**, however, subsequent crits deal increasing damage.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{"Crit: **" + (1 + (crits * CRIT_INCREASE)) + "x**", "Gold Stolen: **" + goldStolen + (!goldTargetHit ? " / " + GOLD_TARGET : "") + "**"};
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.ENERGY, 150 + (goldTargetHit ? 50 : 0))
                .put(Stats.MAX_HEALTH, 735)
                .put(Stats.DAMAGE, 17)
                .put(Stats.HEALTH_PER_TURN, 8)
                .put(Stats.CRIT_CHANCE, 0.2f)
                .put(Stats.CRIT_DAMAGE, -1 * CRIT_REDUCE);

    }

    @Override
    public String onTurnEnd(GameMember member) {
        crits = 0;
        return null;
    }

    @Override
    public DamageEvent critOut(DamageEvent event) {
        event.critMul += crits * CRIT_INCREASE;
        if (crit() == 1) {
            int steal = (int) Math.min((event.actor.getStats().get(Stats.DAMAGE) * STEAL_AD) + (event.actor.getStats().get(Stats.ABILITY_POWER)), event.target.getStats().getInt(Stats.GOLD));

            goldStolen += steal;
            if (goldStolen >= GOLD_TARGET && !goldTargetHit) {
                goldTargetHit = true;
                event.output.add(Emote.ENERGY + "**" + event.actor.getUnit() + "** reached the gold target.");
            }

            event.actor.getStats().add(Stats.GOLD, steal);
            event.target.getStats().sub(Stats.GOLD, steal);
            event.output.add(Emote.BUY + "**" + event.actor.getUsername() + "** stole **" + steal + "** gold!");
        }
        return event;
    }
}
