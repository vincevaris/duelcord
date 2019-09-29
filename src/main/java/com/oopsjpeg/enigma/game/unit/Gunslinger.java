package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;

public class Gunslinger extends Unit {
    public static final float BONUS_DAMAGE = 0.10f;
    public static final float BONUS_AP = 1.5f;
    public static final int BARRAGE_SHOTS = 4;
    public static final float BARRAGE_DAMAGE = 0.2f;
    public static final float BARRAGE_AP = 1.9f;
    public static final int BARRAGE_COOLDOWN = 3;

    public static final String NAME = "Gunslinger";
    public static final String DESC = "The first basic attack per turn always crits and deals **"
            + Util.percent(BONUS_DAMAGE) + "** (" + BONUS_AP + " AP) bonus damage.\n\n"
            + "Using `>barrage` fires **" + BARRAGE_SHOTS + "** shots that each deal **"
            + Util.percent(BARRAGE_DAMAGE) + "** (" + BARRAGE_AP + " AP) base damage.\n"
            + "Barrage shots can crit and apply on-hit effects.\n"
            + "Barrage can only be used once every **" + BARRAGE_COOLDOWN + "** turn(s).";
    public static final Color COLOR = Color.ORANGE;
    public static final Stats STATS = new Stats()
            .put(Stats.ENERGY, 125)
            .put(Stats.MAX_HP, 750)
            .put(Stats.DAMAGE, 17);
    public static final Stats PER_TURN = new Stats()
            .put(Stats.HP, 11)
            .put(Stats.GOLD, 75);

    private boolean bonus = false;
    private int barrage = 0;

    public boolean getBonus() {
        return bonus;
    }

    public void setBonus(boolean bonus) {
        this.bonus = bonus;
    }

    public int getBarrage() {
        return barrage;
    }

    public void setBarrage(int barrage) {
        this.barrage = Util.limit(barrage, -1, BARRAGE_COOLDOWN);
    }

    public boolean canBarrage() {
        return barrage <= 0;
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
        if (!getBonus()) {
            setBonus(true);
            event.crit = true;
            event.bonus += event.damage * (1 + BONUS_DAMAGE) * (1 + (event.actor.getStats().get(Stats.ABILITY_POWER) * BONUS_AP));
        }
        return event;
    }

    @Override
    public String onTurnStart(Game.Member member) {
        setBonus(false);
        setBarrage(getBarrage() - 1);
        if (barrage == 0)
            return Emote.INFO + "**" + member.getName() + "'s Barrage** is ready to use.";
        return "";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDesc() {
        return DESC;
    }

    @Override
    public Color getColor() {
        return COLOR;
    }

    @Override
    public Stats getStats() {
        return STATS;
    }

    @Override
    public Stats getPerTurn() {
        return PER_TURN;
    }
}
