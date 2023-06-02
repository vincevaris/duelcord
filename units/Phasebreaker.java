package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.DebuffSilence;
import com.oopsjpeg.enigma.game.object.Unit;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

public class Phasebreaker extends Unit {
    public static final int FLARE_STACKS = 3;
    public static final float PASSIVE_AP = 0.4f;
    public static final int PHASE_1_AP = 7;
    public static final float PHASE_2_SHIELD = 0.5f;
    public static final int PHASE_2_AP = 8;

    private final Stacker flare = new Stacker(FLARE_STACKS);
    private int phase = 0;
    private boolean flared = false;
    private int bonusAp = 0;

    public Phasebreaker() {
        super("Phasebreaker", new Command[]{new FlareCommand()}, Color.of(0, 255, 191), null);
    }

    @Override
    public String getDescription() {
        return "Attacks deal **" + Util.percent(PASSIVE_AP) + " AP** bonus damage and build **Flare** (up to " + FLARE_STACKS + ")." +
                "\nEach turn consists of a new **Phase**.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{"Phase: **" + phase + "**",
                (flared ? "Flaring!" : "Flare: **" + flare.getCurrent() + " / " + Phasebreaker.FLARE_STACKS + "**"),
                "Bonus AP: **" + bonusAp + "**"};
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.MAX_ENERGY, 125)
                .put(Stats.MAX_HEALTH, 750)
                .put(Stats.DAMAGE, 18)
                .put(Stats.ABILITY_POWER, bonusAp)
                .put(Stats.HEALTH_PER_TURN, 10);
    }

    public void phase() {
        phase++;
        if (phase > 3) phase = 1;
    }

    @Override
    public String onTurnStart(GameMember member) {
        flared = false;
        phase();
        return ":diamond_shape_with_a_dot_inside: It's **Phase " + phase + "**";
    }

    @Override
    public String onDefend(GameMember member) {
        flare.stack();
        return null;
    }

    @Override
    public DamageEvent damageOut(DamageEvent event) {
        event.damage += event.actor.getStats().get(Stats.ABILITY_POWER) * PASSIVE_AP;
        if (flared) {
            switch (phase) {
                case 2:
                    // Shield
                    float ap = event.actor.getStats().get(Stats.ABILITY_POWER) / (PHASE_2_AP * 100);
                    event.shield += event.total() * (PHASE_2_SHIELD + ap);
                    break;
                case 3:
                    // Ignore resist
                    float ignore = 1 + event.target.getStats().get(Stats.RESIST) + (event.target.isDefensive() ? 0.2f : 0);
                    event.damage *= ignore;
                    event.bonus *= ignore;
                    // Double passive
                    event.bonus += event.actor.getStats().get(Stats.ABILITY_POWER) * PASSIVE_AP;
                    event = event.actor.ability(event);
                    break;
            }
        }
        return event;
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        flare.stack();
        if (flared && phase == 1) {
            bonusAp += PHASE_1_AP;
            flare.stack();
            event.actor.getStats().add(Stats.ABILITY_POWER, PHASE_1_AP);
        }
        return event;
    }

    public static class FlareCommand implements Command {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                Phasebreaker unit = (Phasebreaker) member.getUnit();
                message.delete().block();
                if (member.hasData(DebuffSilence.class))
                    Util.sendFailure(channel, "You cannot use **Flare** while silenced.");
                else if (unit.flared)
                    Util.sendFailure(channel, "You already using **Flare**.");
                else if (!unit.flare.isDone())
                    Util.sendFailure(channel, "**Flare** is not ready yet.");
                else
                    member.act(new FlareAction());
            }
        }

        @Override
        public String getName() {
            return "flare";
        }

        @Override
        public String getDescription() {
            return "Gain a buff this turn based on the current **Phase**:" +
                    "\n> **1**. Attacks permanently increase ability power by **" + PHASE_1_AP + "** and grant double **Flare**." +
                    "\n> **2**. Attacks shield for **" + Util.percent(PHASE_2_SHIELD) + "** (+1% per " + PHASE_2_AP + " AP) of damage dealt."  +
                    "\n> **3**. Passive damage is doubled and attacks ignore resist." +
                    "\n**Flare** skips the next phase.";
        }
    }

    public static class FlareAction implements GameAction {
        @Override
        public String act(GameMember actor) {
            Phasebreaker unit = (Phasebreaker) actor.getUnit();
            int phase = unit.phase;
            unit.flare.reset();
            unit.flared = true;
            unit.phase();
            return ":diamond_shape_with_a_dot_inside: **" + actor.getUsername() + "** used **Flare** on **Phase " + phase + "**!";
        }

        @Override
        public int getEnergy() {
            return 0;
        }
    }
}
