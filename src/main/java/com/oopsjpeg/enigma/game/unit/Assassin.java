package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;

public class Assassin extends Unit {
    public static final float POTENCY_STORE = 0.2f;
    public static final int POTENCY_TURNS = 5;
    public static final float SLASH_DAMAGE = 0.3f;
    public static final float SLASH_AP = 0.2f;
    public static final int SLASH_MAX = 4;
    public static final int SILENCE_TURNS = 1;

    public static final String NAME = "Assassin";
    public static final String DESC = "**" + Util.percent(POTENCY_STORE) + "**"
            + " of damage dealt in the last turn is stored as **Potency**."
            + " This can only occur **" + POTENCY_TURNS + "** times until **Potency** is reset."
            + "\n\nUsing `>slash` deals **" + Util.percent(SLASH_DAMAGE) + "** (+" + Util.percent(SLASH_AP) + " AP) of base damage."
            + " Every **" + SLASH_MAX + "th** slash applies **Silence** for **" + SILENCE_TURNS + "** turn(s) and deals"
            + " bonus damage equal to the total **Potency**, resetting it as well."
            + "\n\nSlash does not count towards total **Potency**.";
    public static final Color COLOR = Color.BLUE;
    public static final Stats STATS = new Stats()
            .put(Stats.ENERGY, 125)
            .put(Stats.MAX_HP, 720)
            .put(Stats.DAMAGE, 24);
    public static final Stats PER_TURN = new Stats()
            .put(Stats.HP, 11);

    private boolean slashed = false;
    private final Stacker slash = new Stacker(SLASH_MAX);
    private final Stacker potency = new Stacker(POTENCY_TURNS);
    private float potencyTotal = 0;

    public boolean getSlashed() {
        return slashed;
    }

    public void setSlashed(boolean slashed) {
        this.slashed = slashed;
    }

    public Stacker getSlash() {
        return slash;
    }

    public Stacker getPotency() {
        return potency;
    }

    public float getPotencyTotal() {
        return potencyTotal;
    }

    public void setPotencyTotal(float potencyTotal) {
        this.potencyTotal = potencyTotal;
    }

    @Override
    public String onTurnEnd(Game.Member member) {
        slashed = false;
        if (potency.stack())
            return Emote.KNIFE + "**" + member.getName() + "'s Potency** is at max capacity.";
        return "";
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
        // Assassin potency stacking
        if (!potency.done())
            potencyTotal += event.damage * POTENCY_STORE;
        return event;
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
