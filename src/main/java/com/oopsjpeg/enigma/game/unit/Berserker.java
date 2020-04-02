package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.awt.*;

public class Berserker extends Unit {
    public static final int RAGE_MAX = 5;
    public static final float BONUS_DAMAGE = 0.05f;
    public static final int BONUS_AP = 8;
    public static final int BONUS_ENERGY = 100;

    public static final Stats PER_TURN = new Stats()
            .put(Stats.HEALTH, 12);

    private final Stacker rage = new Stacker(RAGE_MAX);
    private float bonus = 0;

    public Stacker getRage() {
        return rage;
    }

    public String rage(GameMember member) {
        if (rage.stack() && rage.tryNotify())
            return Emote.RAGE + "**" + member.getUsername() + "'s Rage** is at max capacity.";
        return null;
    }

    public float getBonus() {
        return bonus;
    }

    public void setBonus(float bonus) {
        this.bonus = bonus;
    }

    @Override
    public String getName() {
        return "Berserker";
    }

    @Override
    public String getDescription() {
        return "Basic attacking or being basic attacked builds up to **" + RAGE_MAX + "** stacks of **Rage**."
                + "\nUsing `>rage` consumes stacks to increase damage dealt for one turn (**" + Util.percent(BONUS_DAMAGE) + "** (+1% per " + BONUS_AP + " AP) per stack)."
                + "\nAt maximum stacks, Rage grants **" + BONUS_ENERGY + "** bonus energy.";
    }

    @Override
    public Command[] getCommands() {
        return new Command[]{new RageCommand()};
    }

    @Override
    public String[] getTopic() {
        return new String[]{(bonus > 0 ? "Bonus: **" + Util.percent(getBonus()) + "**" : "Rage: **" + getRage().getCurrent() + " / 5**")};
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.ENERGY, 100)
                .put(Stats.MAX_HEALTH, 780)
                .put(Stats.DAMAGE, 19)
                .put(Stats.HEALTH_PER_TURN, 12);
    }

    @Override
    public String onTurnEnd(GameMember member) {
        bonus = 0;
        return null;
    }

    @Override
    public DamageEvent damageOut(DamageEvent event) {
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

    public class RageCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (member.hasData(Silence.class))
                    Util.sendFailure(channel, "You cannot **Rage** while silenced.");
                else if (getRage().getCurrent() == 0)
                    Util.sendFailure(channel, "You cannot **Rage** without any stacks.");
                else
                    member.act(new RageAction());
            }
        }

        @Override
        public String[] getAliases() {
            return new String[]{"rage"};
        }
    }

    public class RageAction implements GameAction {
        @Override
        public String act(GameMember actor) {
            float stack = Berserker.BONUS_DAMAGE + (actor.getStats().get(Stats.ABILITY_POWER) / (Berserker.BONUS_AP * 100));

            setBonus(stack * getRage().getCurrent());

            if (getRage().getCurrent() == Berserker.RAGE_MAX)
                actor.getStats().add(Stats.ENERGY, 100);

            getRage().reset();

            return Emote.RAGE + "**" + actor.getUsername() + "** has gained **" + Util.percent(getBonus()) + "** more damage "
                    + (getRage().getCurrent() == Berserker.RAGE_MAX ? "and **100** energy " : "") + "this turn!";
        }

        @Override
        public int getEnergy() {
            return 0;
        }
    }
}
