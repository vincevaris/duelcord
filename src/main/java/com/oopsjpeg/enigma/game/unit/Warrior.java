package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;

public class Warrior extends Unit {
    public static final int BONUS_MAX = 3;
    public static final float BONUS_DAMAGE = 0.3f;
    public static final float BASH_DAMAGE = 0.5f;

    public static final String NAME = "Warrior";
    public static final String DESC = "Every **" + BONUS_MAX + "rd** attack deals **" + Util.percent(BONUS_DAMAGE) + "** bonus damage."
            + "\nUsing `>bash` breaks shield and resistance then deals **" + Util.percent(BASH_DAMAGE) + "** of base damage."
            + "\nBash counts towards stacks of bonus damages, but does not proc it.";
    public static final Color COLOR = Color.CYAN;
    public static final Stats STATS = new Stats()
            .put(Stats.ENERGY, 125)
            .put(Stats.MAX_HP, 795)
            .put(Stats.DAMAGE, 24);
    public static final Stats PER_TURN = new Stats()
            .put(Stats.HP, 13);

    private Stacker bonus = new Stacker(BONUS_MAX);
    private boolean bash = false;

    public Stacker getBonus() {
        return bonus;
    }

    public boolean getBash() {
        return bash;
    }

    public void setBash(boolean bash) {
        this.bash = bash;
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
    public String onTurnEnd(Game.Member member) {
        bash = false;
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
