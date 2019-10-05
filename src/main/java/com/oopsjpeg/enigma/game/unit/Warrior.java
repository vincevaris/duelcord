package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;

public class Warrior extends Unit {
    public static final int BONUS_MAX = 3;
    public static final float BONUS_DAMAGE = 0.3f;
    public static final float BASH_DAMAGE = 0.5f;
    public static final float BASH_HP_SCALE = 0.25f;
    public static final int BASH_COOLDOWN = 2;

    public static final String NAME = "Warrior";
    public static final String DESC = "Every **" + BONUS_MAX + "rd** attack deals **" + Util.percent(BONUS_DAMAGE) + "** bonus damage."
            + "\n\nUsing `>bash` breaks the target's shield and resist then deals **" + Util.percent(BASH_DAMAGE) + "** base damage (+" + Util.percent(BASH_HP_SCALE) + " bonus max health)."
            + "\n**Bash** counts towards stacks of bonus damages, but does not proc it."
            + "\n**Bash** can only be used once every **" + BASH_COOLDOWN + "** turn(s).";
    public static final Color COLOR = Color.CYAN;
    public static final Stats STATS = new Stats()
            .put(Stats.ENERGY, 125)
            .put(Stats.MAX_HEALTH, 795)
            .put(Stats.DAMAGE, 23);
    public static final Stats PER_TURN = new Stats()
            .put(Stats.HEALTH, 13);

    private Stacker bonus = new Stacker(BONUS_MAX);
    private Cooldown bash = new Cooldown(BASH_COOLDOWN);

    public Stacker getBonus() {
        return bonus;
    }

    public Cooldown getBash() {
        return bash;
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

    @Override
    public String onTurnStart(Game.Member member) {
        if (bash.count() && bash.notif())
            return Emote.INFO + "**" + member.getUsername() + "'s Bash** is ready to use.";
        return "";
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
        if (bonus.stack()) {
            event.bonus += event.actor.getStats().get(Stats.DAMAGE) * BONUS_DAMAGE;
            bonus.reset();
        }
        return event;
    }
}
