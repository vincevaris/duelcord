package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.DebuffSilence;
import com.oopsjpeg.enigma.game.buff.DebuffWound;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.game.object.Unit;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

import java.util.ArrayList;

import static com.oopsjpeg.enigma.game.Stats.MAX_ENERGY;

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

    private final Stacker wound = new Stacker(REAP_WOUND_USES);
    private final Cooldown endure = new Cooldown(ENDURE_COOLDOWN);

    private float soul = 0;

    public Bloodreaper() {
        super("Bloodreaper", new Command[]{new ReapCommand(), new EndureCommand()}, Color.of(120, 0, 0), new Stats()
                .put(MAX_ENERGY, 125)
                .put(Stats.MAX_HEALTH, 720)
                .put(Stats.HEALTH_PER_TURN, 7)
                .put(Stats.DAMAGE, 14));

    }

    @Override
    public String getDescription() {
        return "**" + Util.percent(SOUL_RATIO) + "** of damage received is stored as **Soul**, up to **" + SOUL_MAX + "** (+" + Util.percent(SOUL_MAX_HP_RATIO) + " bonus health)." +
                "\nUnbroken shields heal for **" + Util.percent(SHIELD_HEAL) + "** of their value.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{"Soul: **" + Math.round(soul) + "**",
                "Wound: **" + wound.getCurrent() + " / " + REAP_WOUND_USES + "**",
                endure.isDone() ? "Endure is ready" : "Endure in **" + endure.getCurrent() + "** turn(s)"};
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
        if (member.hasShield())
            output.add(member.heal(member.getShield() * SHIELD_HEAL, "Lifeforce"));
        return String.join("\n", output);
    }

    private static class ReapCommand implements Command {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (member.hasData(DebuffSilence.class))
                    Util.sendFailure(channel, "You cannot **Reap** while silenced.");
                else
                    member.act(new ReapAction(game.getRandomTarget(member)));
            }
        }

        @Override
        public String getName() {
            return "reap";
        }

        @Override
        public String getDescription() {
            return "Deals **" + REAP_DAMAGE + "** (+" + Util.percent(REAP_DAMAGE_AP_RATIO) + " AP) damage and heals for **" + Util.percent(REAP_HEAL) + "** of damage dealt." +
                    "\nEvery **" + REAP_WOUND_USES + "** use applies **Wound** by **" + Util.percent(REAP_WOUND) + "** for **1** turn.";
        }
    }

    private static class EndureCommand implements Command {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                Bloodreaper unit = (Bloodreaper) member.getUnit();
                if (!unit.endure.isDone())
                    Util.sendFailure(channel, "**Endure** is on cooldown for **" + unit.endure.getCurrent() + "** more turn(s).");
                else if (unit.soul <= 0)
                    Util.sendFailure(channel, "You do not have any **Soul**.");
                else
                    member.act(new EndureAction());
            }
        }

        @Override
        public String getName() {
            return "endure";
        }

        @Override
        public String getDescription() {
            return "Consumes **Soul**, shielding equal to its amount and restoring **25** energy.";
        }
    }

    private static class ReapAction implements GameAction {
        private final GameMember target;

        public ReapAction(GameMember target) {
            this.target = target;
        }

        @Override
        public String act(GameMember actor) {
            Bloodreaper unit = (Bloodreaper) actor.getUnit();
            unit.wound.stack();

            DamageEvent event = new DamageEvent(actor.getGame(), actor, target);
            event.damage = REAP_DAMAGE + (actor.getStats().get(Stats.ABILITY_POWER) * REAP_DAMAGE_AP_RATIO);
            event.output.add(actor.heal(event.damage * REAP_HEAL, "Reap"));

            if (unit.wound.isDone()) {
                unit.wound.reset();
                event.output.add(target.buff(new DebuffWound(actor, 1, REAP_WOUND)));
            }

            event = actor.ability(event);

            return actor.damage(event, Emote.KNIFE, "Reap");
        }

        @Override
        public int getEnergy() {
            return 50;
        }

        public GameMember getTarget() {
            return this.target;
        }
    }

    private static class EndureAction implements GameAction {
        @Override
        public String act(GameMember actor) {
            Bloodreaper unit = (Bloodreaper) actor.getUnit();
            ArrayList<String> output = new ArrayList<>();

            output.add(Emote.USE + "**" + actor.getUsername() + "** used **Endure**!");

            // Shield
            output.add(actor.shield(unit.soul));
            // Remove debuffs
            if (actor.getData().stream().anyMatch(o -> o instanceof Buff && ((Buff) o).isDebuff()))
                actor.getData().removeIf(o -> o instanceof Buff && ((Buff) o).isDebuff());
            // Restore energy
            if (actor.getEnergy() < unit.getStats().get(MAX_ENERGY)) {
                output.add(Emote.ENERGY + "**" + actor.getUsername() + "** restored **" + ENDURE_ENERGY + "** energy.");
                actor.giveEnergy(ENDURE_ENERGY);
            }

            unit.soul = 0;
            unit.endure.start();

            return Util.joinNonEmpty("\n", output);
        }

        @Override
        public int getEnergy() {
            return 0;
        }
    }
}