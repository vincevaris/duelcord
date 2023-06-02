package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.DebuffSilence;
import com.oopsjpeg.enigma.game.object.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

public class Berserker extends Unit {
    public static final int RAGE_MAX = 5;
    public static final float BONUS_DAMAGE = 0.05f;
    public static final int BONUS_AP = 8;
    public static final int BONUS_ENERGY = 100;

    private final Stacker rage = new Stacker(RAGE_MAX);
    private float bonus = 0;

    public Berserker() {
        super("Berserker", new Command[]{new RageCommand()}, Color.RED, null);
    }

    public String rage(GameMember member) {
        if (rage.stack() && rage.tryNotify())
            return Emote.RAGE + "**" + member.getUsername() + "'s Rage** is at max capacity.";
        return null;
    }

    @Override
    public String getDescription() {
        return "Attacking or being attacked builds **Rage** (up to **" + RAGE_MAX + "**).";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{(bonus > 0 ? "Bonus: **" + Util.percent(bonus) + "**" : "Rage: **" + rage.getCurrent() + " / 5**")};
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.MAX_ENERGY, 100)
                .put(Stats.MAX_HEALTH, 760)
                .put(Stats.DAMAGE, 19)
                .put(Stats.HEALTH_PER_TURN, 10)
                .put(Stats.RESIST, bonus == 0 ? 0.2f : 0);
    }

    @Override
    public String onTurnStart(GameMember member) {
        if (bonus > 0) {
            bonus = 0;
            member.updateStats();
        }
        return null;
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        if (bonus > 0)
            event.damage *= 1 + bonus;
        else
            event.output.add(rage(event.actor));
        return event;
    }

    @Override
    public DamageEvent basicAttackIn(DamageEvent event) {
        event.output.add(rage(event.target));
        return event;
    }

    public Stacker getRage() {
        return rage;
    }

    public float getBonus() {
        return bonus;
    }

    public void setBonus(float bonus) {
        this.bonus = bonus;
    }

    public static class RageCommand implements Command {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                Berserker unit = (Berserker) member.getUnit();
                if (member.hasData(DebuffSilence.class))
                    Util.sendFailure(channel, "You cannot **Rage** while silenced.");
                else if (unit.rage.getCurrent() == 0)
                    Util.sendFailure(channel, "You cannot **Rage** without any stacks.");
                else
                    member.act(new RageAction());
            }
        }

        @Override
        public String getName() {
            return "rage";
        }

        @Override
        public String getDescription() {
            return "Consumes stacks to increase damage dealt for one turn (**" + Util.percent(BONUS_DAMAGE) + "** (+1% per " + BONUS_AP + " AP) per stack)." +
                    "\nAt **" + RAGE_MAX + "** stacks, **Rage** grants **" + BONUS_ENERGY + "** bonus energy.";
        }
    }

    public static class RageAction implements GameAction {
        @Override
        public String act(GameMember actor) {
            Berserker unit = (Berserker) actor.getUnit();
            float stack = Berserker.BONUS_DAMAGE + (actor.getStats().get(Stats.ABILITY_POWER) / (Berserker.BONUS_AP * 100));

            unit.bonus = stack * unit.rage.getCurrent();

            if (unit.rage.getCurrent() == Berserker.RAGE_MAX)
                actor.getStats().add(Stats.MAX_ENERGY, 100);

            unit.rage.reset();

            actor.updateStats();

            return Emote.RAGE + "**" + actor.getUsername() + "** has gained **" + Util.percent(unit.bonus) + "** more damage "
                    + (unit.rage.getCurrent() == Berserker.RAGE_MAX ? "and **100** energy " : "") + "this turn!";
        }

        @Override
        public int getEnergy() {
            return 0;
        }
    }
}
