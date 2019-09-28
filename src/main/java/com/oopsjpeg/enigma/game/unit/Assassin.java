package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

import java.awt.*;

public class Assassin extends Unit {
    public static final float POTENCY_STACK_MIN = 0.1f;
    public static final float POTENCY_STACK_MAX = 0.3f;
    public static final int POTENCY_TURNS = 5;
    public static final float SLASH_DAMAGE = 0.3f;
    public static final int SLASH_STACK_MAX = 4;
    public static final int SILENCE_TURNS = 1;

    public static final String NAME = "Assassin";
    public static final String DESC = "**" + Util.percent(POTENCY_STACK_MIN) + "-" + Util.percent(POTENCY_STACK_MAX) + "**"
            + " of damage dealt in the last turn is stored as **Potency**."
            + " This can only occur **" + POTENCY_TURNS + "** times until **Potency** is reset."
            + "\n\nUsing `>slash` deals **" + Util.percent(SLASH_DAMAGE) + "** of base damage."
            + " Every fourth `>slash` **silences** the target for **" + SILENCE_TURNS + "** turn(s) and deals"
            + " bonus damage equal to the total **Potency**, resetting it as well."
            + "\n\n`>slash` does not count towards total **Potency**.";
    public static final Color COLOR = Color.BLUE;
    public static final Stats STATS = new Stats()
            .put(Stats.ENERGY, 125)
            .put(Stats.MAX_HP, 720)
            .put(Stats.DAMAGE, 24)
            .put(Stats.ABILITY_POWER, 1);
    public static final Stats PER_TURN = new Stats()
            .put(Stats.HP, 11)
            .put(Stats.GOLD, 75);

    private boolean slashed = false;
    private int slashCount = 0;
    private float potency = 0;
    private float potencyNow = 0;
    private int potencyTurn = 0;

    public boolean getSlashed() {
        return slashed;
    }

    public void setSlashed(boolean slashed) {
        this.slashed = slashed;
    }

    public int getSlashCount() {
        return slashCount;
    }

    public void setSlashCount(int slashCount) {
        this.slashCount = Util.limit(slashCount, 0, 4);
    }

    public int slashCount() {
        slashCount++;
        return slashCount;
    }

    public float getPotency() {
        return potency;
    }

    public void setPotency(float potency) {
        this.potency = potency;
    }

    public void addPotency(float potency) {
        this.potency += potency;
    }

    public float getPotencyNow() {
        return potencyNow;
    }

    public void setPotencyNow(float potencyNow) {
        this.potencyNow = potencyNow;
    }

    public void addPotencyNow(float potencyNow) {
        this.potencyNow += potencyNow;
    }

    public int getPotencyTurn() {
        return potencyTurn;
    }

    public void setPotencyTurn(int potencyTurn) {
        this.potencyTurn = potencyTurn;
    }

    @Override
    public String onTurnEnd(Game.Member member) {
        slashed = false;
        if (potencyNow > 0 && potencyTurn < 5) {
            potencyNow = 0;
            potencyTurn++;
        }
        if (potencyTurn == 5)
            return Emote.KNIFE + "**" + member.getName() + "'s Potency** is at maximum capacity.";
        return "";
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
        // Assassin potency stacking
        if (potencyTurn < Assassin.POTENCY_TURNS) {
            float potency = event.damage * Math.min(Assassin.POTENCY_STACK_MAX, Assassin.POTENCY_STACK_MIN + (event.game.getTurnCount() * 0.005f));
            addPotency(potency);
            addPotencyNow(potency);
        }

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
