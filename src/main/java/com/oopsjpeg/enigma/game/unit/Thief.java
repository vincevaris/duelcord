package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;

public class Thief extends Unit {
    public static final float STEAL_AMOUNT = 0.3f;
    public static final float CRIT_REDUCE = 0.2f;
    public static final float CRIT_INCREASE = 0.2f;

    private int critAmount = 0;
    private int goldStolen = 0;

    public int getCritAmount() {
        return critAmount;
    }

    public void setCritAmount(int critAmount) {
        this.critAmount = Math.max(0, critAmount);
    }

    public int crit() {
        setCritAmount(critAmount + 1);
        return critAmount;
    }

    @Override
    public DamageEvent critOut(DamageEvent event) {
        event.critMul += getCritAmount() * CRIT_INCREASE;
        if (crit() == 1) {
            int steal = (int) Math.min((event.actor.getStats().get(Stats.DAMAGE) * STEAL_AMOUNT) + (event.actor.getStats().get(Stats.ABILITY_POWER)), event.target.getStats().getInt(Stats.GOLD));
            goldStolen += steal;
            event.actor.getStats().add(Stats.GOLD, steal);
            event.target.getStats().sub(Stats.GOLD, steal);
            event.output.add(Emote.BUY + "**" + event.actor.getUsername() + "** stole **" + steal + "** gold!");
        }
        return event;
    }

    @Override
    public String getName() {
        return "Thief";
    }

    @Override
    public String getDescription() {
        return "The first crit per turn steals gold equal to **" + Util.percent(STEAL_AMOUNT) + "** base damage."
                + "\nCrit damage is reduced by **" + Util.percent(CRIT_REDUCE) + "**, however, subsequent crits in a turn deal increasing damage.";
    }

    @Override
    public String[] getTopic() {
        return new String[]{"Crit: **" + (1 + (critAmount * CRIT_INCREASE)) + "x**", "Gold Stolen: **" + goldStolen + "**"};
    }

    @Override
    public Color getColor() {
        return Color.YELLOW;
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.ENERGY, 150)
                .put(Stats.MAX_HEALTH, 740)
                .put(Stats.DAMAGE, 22)
                .put(Stats.CRIT_CHANCE, 0.25f)
                .put(Stats.CRIT_DAMAGE, -1 * CRIT_REDUCE)
                .put(Stats.HEALTH_PER_TURN, 8);
    }

    @Override
    public String onTurnEnd(GameMember member) {
        critAmount = 0;
        return null;
    }
}
