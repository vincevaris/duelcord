package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.buff.Bleed;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;

public class Duelist extends Unit {
    public static final int BONUS_MAX = 4;
    public static final float BONUS_DAMAGE = 0.02f;
    public static final float BLEED_DAMAGE = 0.5f;
    public static final int BLEED_TURNS = 2;
    public static final float CRUSH_POWER = 0.2f;
    public static final int CRUSH_TURNS = 1;
    public static final int CRUSH_EXTEND = 1;
    public static final int CRUSH_COOLDOWN = 3;

    public static final String NAME = "Duelist";
    public static final String DESC = "Every **" + BONUS_MAX + "th** basic attack deals bonus damage equal to **"
            + Util.percent(BONUS_DAMAGE) + "** of the target's max health and applies **Bleed** for **"
            + Util.percent(BLEED_DAMAGE) + "** of base damage for **" + BLEED_TURNS + "** turn(s).\n\n"
            + "Using `>crush` weakens the target by **" + Util.percent(CRUSH_POWER) + "** for **" + CRUSH_TURNS + "** turn(s).\n"
            + "If the target receives any other debuff while weakened, it is extended by **" + CRUSH_EXTEND + "** turn(s).\n"
            + "Crush can only be used once every **" + CRUSH_COOLDOWN + "** turn(s).";
    public static final Color COLOR = Color.MAGENTA;
    public static final Stats STATS = new Stats()
            .put(Stats.ENERGY, 125)
            .put(Stats.MAX_HP, 750)
            .put(Stats.DAMAGE, 25);
    public static final Stats PER_TURN = new Stats()
            .put(Stats.HP, 14);

    private final Stacker bonus = new Stacker(BONUS_MAX);
    private final Cooldown crush = new Cooldown(CRUSH_COOLDOWN);

    public Stacker getBonus() {
        return bonus;
    }

    public Cooldown getCrush() {
        return crush;
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
        if (bonus.stack()) {
            bonus.reset();
            float bonus = event.target.getStats().getInt(Stats.MAX_HP) * BONUS_DAMAGE;
            float bleed = event.actor.getStats().get(Stats.DAMAGE) * BLEED_DAMAGE;
            event.bonus += bonus;
            event.output.add(event.target.buff(new Bleed(event.actor, BLEED_TURNS, bleed)));
        }
        return event;
    }

    @Override
    public String onTurnStart(Game.Member member) {
        if (crush.count() && crush.notif())
            return Emote.INFO + "**" + member.getUsername() + "'s Crush** is ready to use.";
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
