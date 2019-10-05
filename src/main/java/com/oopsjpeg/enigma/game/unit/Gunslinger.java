package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;

public class Gunslinger extends Unit {
    public static final float BONUS_DAMAGE = 0.2f;
    public static final float BONUS_AP = 1.2f;
    public static final int BARRAGE_SHOTS = 4;
    public static final float BARRAGE_DAMAGE = 0.2f;
    public static final float BARRAGE_AP = 0.3f;
    public static final int BARRAGE_COOLDOWN = 3;

    public static final String NAME = "Gunslinger";
    public static final String DESC = "The first basic attack per turn always crits and deals **"
            + Util.percent(BONUS_DAMAGE) + "** (+" + Util.percent(BONUS_AP) + " AP) bonus damage.\n\n"
            + "Using `>barrage` fires **" + BARRAGE_SHOTS + "** shots that each deal **"
            + Util.percent(BARRAGE_DAMAGE) + "** base damage (+" + Util.percent(BARRAGE_AP) + " AP).\n"
            + "Barrage shots can crit and apply on-hit effects.\n"
            + "Barrage can only be used once every **" + BARRAGE_COOLDOWN + "** turn(s).";
    public static final Color COLOR = new Color(255, 100, 0);
    public static final Stats STATS = new Stats()
            .put(Stats.ENERGY, 125)
            .put(Stats.MAX_HEALTH, 750)
            .put(Stats.DAMAGE, 17);
    public static final Stats PER_TURN = new Stats()
            .put(Stats.HEALTH, 11);

    private boolean bonus = false;
    private final Cooldown barrage = new Cooldown(BARRAGE_COOLDOWN);

    public boolean getBonus() {
        return bonus;
    }

    public void setBonus(boolean bonus) {
        this.bonus = bonus;
    }

    public Cooldown getBarrage() {
        return barrage;
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
        if (!getBonus()) {
            setBonus(true);
            event.crit = true;
            event.bonus += (event.damage * BONUS_DAMAGE) + (event.actor.getStats().get(Stats.ABILITY_POWER) * BONUS_AP);
        }
        return event;
    }

    @Override
    public String onTurnStart(Game.Member member) {
        setBonus(false);
        if (barrage.count() && barrage.notif())
            return Emote.INFO + "**" + member.getUsername() + "'s Barrage** is ready to use.";
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
