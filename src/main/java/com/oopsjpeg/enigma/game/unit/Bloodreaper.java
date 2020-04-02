package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.game.buff.Wound;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;

public class Bloodreaper extends Unit {
    public static final float SOUL_RATIO = 0.2f;
    public static final int SOUL_MAX = 50;
    public static final float SOUL_MAX_HP_RATIO = 0.2f;
    public static final float SHIELD_HEAL = 0.3f;
    public static final int REAP_DAMAGE = 15;
    public static final float REAP_DAMAGE_AP_RATIO = 0.6f;
    public static final float REAP_HEAL = 0.25f;
    public static final float REAP_WOUND = 0.6f;
    public static final int REAP_WOUND_USES = 4;
    public static final int ENDURE_ENERGY = 25;
    public static final int ENDURE_COOLDOWN = 3;

    @Getter public static final Stacker wound = new Stacker(REAP_WOUND_USES);
    @Getter private final Cooldown endure = new Cooldown(ENDURE_COOLDOWN);

    @Getter @Setter private float soul = 0;

    @Override
    public String getName() {
        return "Bloodreaper";
    }

    @Override
    public String getDescription() {
        return "**" + Util.percent(SOUL_RATIO) + "** of damage received is stored as **Soul**, up to **" + SOUL_MAX + "** (+" + Util.percent(SOUL_MAX_HP_RATIO) + " bonus health)."
                + "\nUnbroken shields heal for **" + Util.percent(SHIELD_HEAL) + "** of their value."
                + "\n\n`>reap` deals **" + REAP_DAMAGE + "** (+" + Util.percent(REAP_DAMAGE_AP_RATIO) + " AP) damage and heals for **" + Util.percent(REAP_HEAL) + "**."
                + "\nEvery **" + REAP_WOUND_USES + "** uses, Reap applies Wound by **" + Util.percent(REAP_WOUND) + "** for **1** turn."
                + "\n\n`>endure` resets **Soul**, shielding equal to its amount."
                + "\nAdditionally, it removes all de-buffs and restores **" + ENDURE_ENERGY + "** energy."
                + "\nEndure can only be used once every **" + ENDURE_COOLDOWN + "** turn(s).";
    }

    @Override
    public Color getColor() {
        return new Color(120, 0, 0);
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.MAX_HEALTH, 720)
                .put(Stats.HEALTH_PER_TURN, 10)
                .put(Stats.DAMAGE, 14)
                .put(Stats.ENERGY, 125);
    }

    @Override
    public Command[] getCommands() {
        return new Command[]{new ReapCommand(), new EndureCommand()};
    }

    @Override
    public String[] getTopic() {
        return new String[]{"Soul: **" + Math.round(soul) + "**", endure.isDone() ? "Endure is ready." : "Endure in **" + endure.getCurrent() + "** turn(s)."};
    }

    @Override
    public DamageEvent damageIn(DamageEvent event) {
        soul = Util.limit(soul + (event.total() * SOUL_RATIO), 0, SOUL_MAX + (event.target.getBonusHealth() * SOUL_MAX_HP_RATIO));
        return event;
    }

    @Override
    public String onTurnStart(GameMember member) {
        ArrayList<String> output = new ArrayList<>();
        if (endure.count() && endure.tryNotify())
            output.add(Emote.INFO + "**" + member.getUsername() + "**'s Endure is ready to use.");
        if (member.getStats().get(Stats.SHIELD) > 0)
            output.add(member.heal(member.getStats().get(Stats.SHIELD) * SHIELD_HEAL, "Lifeforce"));
        return String.join("\n", output);
    }

    private class ReapCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (member.hasData(Silence.class))
                    Util.sendFailure(channel, "You cannot **Reap** while silenced.");
                else
                    member.act(new ReapAction(game.getRandomTarget(member)));
            }
        }

        @Override
        public String[] getAliases() {
            return new String[]{"reap"};
        }
    }

    private class EndureCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (!endure.isDone())
                    Util.sendFailure(channel, "**Endure** is on cooldown for **" + endure.getCurrent() + "** more turn(s).");
                else if (soul <= 0)
                    Util.sendFailure(channel, "You do not have any **Soul**.");
                else
                    member.act(new EndureAction());
            }
        }

        @Override
        public String[] getAliases() {
            return new String[]{"endure"};
        }
    }

    @RequiredArgsConstructor
    private class ReapAction implements GameAction {
        @Getter private final GameMember target;

        @Override
        public String act(GameMember actor) {
            wound.stack();

            DamageEvent event = new DamageEvent(actor.getGame(), actor, target);
            event.damage = REAP_DAMAGE + (actor.getStats().get(Stats.ABILITY_POWER) * REAP_DAMAGE_AP_RATIO);
            event.output.add(actor.heal(event.damage * REAP_HEAL, "Reap"));

            if (wound.isDone()) {
                wound.reset();
                event.output.add(target.buff(new Wound(actor, 1, REAP_WOUND)));
            }

            event = actor.ability(event);

            return actor.damage(event, Emote.KNIFE, "Reap");
        }

        @Override
        public int getEnergy() {
            return 50;
        }
    }

    private class EndureAction implements GameAction {
        @Override
        public String act(GameMember actor) {
            ArrayList<String> output = new ArrayList<>();

            output.add(Emote.USE + "**" + actor.getUsername() + "** used **Endure**!");

            // Shield
            output.add(actor.shield(soul));
            // Remove debuffs
            if (actor.getData().stream().anyMatch(o -> o instanceof Buff && ((Buff) o).isDebuff()))
                actor.getData().removeIf(o -> o instanceof Buff && ((Buff) o).isDebuff());
            // Restore energy
            if (actor.getStats().get(Stats.ENERGY) < getStats().get(Stats.ENERGY)) {
                output.add(Emote.ENERGY + "**" + actor.getUsername() + "** restored **" + ENDURE_ENERGY + "** energy.");
                actor.getStats().add(Stats.ENERGY, ENDURE_ENERGY);
            }

            soul = 0;
            endure.start();

            return Util.joinNonEmpty(output);
        }

        @Override
        public int getEnergy() {
            return 0;
        }
    }
}