package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.awt.*;

public class Blademaster extends Unit {
    public static final int GEMBLADE_MAX = 5;
    public static final float GEMBLADE_DAMAGE = 0.02f;
    public static final int GEMBLADE_AD_SCALE = 15;
    public static final int GEMBLADE_ENERGY = 50;
    public static final float REFLECT_DAMAGE = 0.15f;
    public static final float REFLECT_SCALE = 0.05f;
    public static final int REFLECT_COOLDOWN = 3;

    public static final int NONE = 0;
    public static final int REFLECTED = 1;
    public static final int REFLECTING = 2;

    private final Stacker gemblade = new Stacker(GEMBLADE_MAX);
    private final Cooldown reflect = new Cooldown(REFLECT_COOLDOWN);
    private int reflectState = 0;

    public String notif(Game.Member member) {
        return Emote.INFO + "**" + member.getUsername() + "'s Gemblade** is at max capacity.";
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        float scale = GEMBLADE_DAMAGE + (event.actor.getStats().get(Stats.DAMAGE) / (GEMBLADE_AD_SCALE * 100));
        float bonus = event.damage * (gemblade.getCurrent() * scale);
        event.bonus += bonus;

        if (gemblade.stack() && gemblade.isDone() && gemblade.tryNotify())
            event.output.add(notif(event.actor));

        return event;
    }

    @Override
    public DamageEvent basicAttackIn(DamageEvent event) {
        if (reflectState == REFLECTING) {
            Game.Member swapActor = event.game.new Member(event.target);
            Game.Member swapTarget = event.game.new Member(event.actor);

            event.actor = swapActor;
            event.target = swapTarget;

            event.bonus += event.actor.getStats().get(Stats.DAMAGE) * (REFLECT_DAMAGE + (gemblade.getCurrent() * REFLECT_SCALE));

            event.output.add(Emote.KNIFE + "**" + event.actor.getUsername() + "** reflected the attack!");

            reflectState = REFLECTED;
        }
        return event;
    }

    @Override
    public String onTurnStart(Game.Member member) {
        if (reflectState != NONE) {
            reflectState = NONE;
            gemblade.reset();
        }

        if (gemblade.isDone())
            member.getStats().add(Stats.ENERGY, GEMBLADE_ENERGY);

        if (reflect.count() && reflect.tryNotify())
            return Emote.INFO + "**" + member.getUsername() + "'s Reflect** is ready to use.";
        return "";
    }

    @Override
    public String onTurnEnd(Game.Member member) {
        if (reflectState == REFLECTING)
            return Emote.WARN + "**" + member.getUsername() + "** is **reflecting** the next attack.";
        return "";
    }

    @Override
    public String getName() {
        return "Blademaster";
    }

    @Override
    public String getDescription() {
        return "Basic attacks stack **Gemblade** up to **" + GEMBLADE_MAX + "** times, dealing **" + Util.percent(GEMBLADE_DAMAGE) + "** (+1% per " + GEMBLADE_AD_SCALE + " bonus AD) more damage each stack."
                + " At max stacks, **Gemblade** grants **" + GEMBLADE_ENERGY + "** bonus energy."
                + "\n\n`>reflect` completely reflects the next attack, deals **" + Util.percent(REFLECT_DAMAGE) + "** (+" + Util.percent(REFLECT_SCALE) + " per **Gemblade** stack) bonus base damage, and resets **Gemblade**."
                + "\n**Reflect** expires if it is not proc’d before the next turn."
                + "\n**Reflect** can only be used once every **" + REFLECT_COOLDOWN + "** turns.";
    }

    @Override
    public Command[] getCommands() {
        return new Command[]{new ReflectCommand()};
    }

    @Override
    public String[] getTopic() {
        return new String[]{"Gemblade: **" + gemblade.getCurrent() + " / " + GEMBLADE_MAX + "**"};
    }

    @Override
    public Color getColor() {
        return new Color(255, 110, 140);
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.MAX_HEALTH, 725)
                .put(Stats.DAMAGE, 20)
                .put(Stats.ENERGY, 125)
                .put(Stats.HEALTH_PER_TURN, 10);
    }

    public class ReflectCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            Game.Member member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (member.hasData(Silence.class))
                    Util.sendFailure(channel, "You cannot **Reflect** while silenced.");
                else if (reflectState == REFLECTING)
                    Util.sendFailure(channel, "You are already reflecting.");
                else if (!reflect.isDone())
                    Util.sendFailure(channel, "**Reflect** is on cooldown for **" + reflect.getCurrent() + "** more turn(s).");
                else
                    member.act(new ReflectAction());
            }
        }

        @Override
        public String[] getAliases() {
            return new String[]{"reflect"};
        }
    }

    public class ReflectAction implements GameAction {
        @Override
        public String act(Game.Member actor) {
            reflect.start();
            reflectState = REFLECTING;

            return Util.joinNonEmpty(Emote.USE + "**" + actor.getUsername() + "** used **Reflect**!", actor.defend());
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }
}
