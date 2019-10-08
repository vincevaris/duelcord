package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.awt.*;

public class Berserker extends Unit {
    public static final int RAGE_MAX = 5;
    public static final float BONUS_DAMAGE = 0.04f;
    public static final int BONUS_AP = 10;
    public static final int BONUS_ENERGY = 100;

    public static final Stats PER_TURN = new Stats()
            .put(Stats.HEALTH, 12);

    private final Stacker rage = new Stacker(RAGE_MAX);
    private float bonus = 0;

    public Stacker getRage() {
        return rage;
    }

    public String rage(Game.Member member) {
        if (rage.stack() && rage.notif())
            return Emote.RAGE + "**" + member.getUsername() + "'s Rage** is at max capacity.";
        return "";
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
                + "\nUsing `>rage` consumes stacks to increase damage dealt for a single turn (**" + Util.percent(BONUS_DAMAGE) + "** (+1% per " + BONUS_AP + " AP) per stack)."
                + "\nAt maximum stacks, Rage grants **" + BONUS_ENERGY + "** bonus energy.";
    }

    @Override
    public Command[] getCommands() {
        return new Command[]{new RageCommand()};
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
                .put(Stats.DAMAGE, 19);
    }

    @Override
    public Stats getPerTurn() {
        return new Stats()
                .put(Stats.HEALTH, 12);
    }

    @Override
    public String onTurnEnd(Game.Member member) {
        bonus = 0;
        return "";
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
        if (bonus > 0)
            event.damage *= 1 + bonus;
        else
            event.output.add(rage(event.actor));
        return event;
    }

    @Override
    public DamageEvent wasBasicAttack(DamageEvent event) {
        event.output.add(rage(event.target));
        return event;
    }

    public class RageCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            Game.Member member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (game.getGameState() == 0)
                    Util.sendFailure(channel, "You cannot use **Rage** until the game has started.");
                else
                    member.act(game.new RageAction());
            }
        }

        @Override
        public String getName() {
            return "rage";
        }
    }
}
