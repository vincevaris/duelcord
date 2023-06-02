package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.DebuffBleed;
import com.oopsjpeg.enigma.game.buff.DebuffSilence;
import com.oopsjpeg.enigma.game.buff.DebuffWeaken;
import com.oopsjpeg.enigma.game.object.Unit;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

import static com.oopsjpeg.enigma.util.Util.percent;

public class Duelist extends Unit {
    public static final int BONUS_MAX = 4;
    public static final float BONUS_DAMAGE = 0.03f;
    public static final float BLEED_DAMAGE = 0.4f;
    public static final int BLEED_TURNS = 2;
    public static final float CRUSH_POWER = 0.2f;
    public static final int CRUSH_TURNS = 1;
    public static final int CRUSH_EXTEND = 1;
    public static final int CRUSH_COOLDOWN = 3;

    private final Stacker bonus = new Stacker(BONUS_MAX);
    private final Cooldown crush = new Cooldown(CRUSH_COOLDOWN);

    public Duelist() {
        super("Duelist", new Command[]{new CrushCommand()}, Color.MAGENTA, new Stats()
                .put(Stats.MAX_ENERGY, 125)
                .put(Stats.MAX_HEALTH, 750)
                .put(Stats.DAMAGE, 21)
                .put(Stats.HEALTH_PER_TURN, 10));
    }

    @Override
    public String getDescription() {
        return "Every **" + BONUS_MAX + "th** basic attack deals bonus damage equal to **"
                + percent(BONUS_DAMAGE) + "** of the target's max health and applies **Bleed** by **"
                + percent(BLEED_DAMAGE) + "** base damage for **" + BLEED_TURNS + "** turn(s).";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{"Bonus: **" + bonus.getCurrent() + " / " + Duelist.BONUS_MAX + "**",
                crush.isDone() ? "Crush is ready" : "Crush in **" + crush.getCurrent() + "** turn(s)"};
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        if (bonus.stack()) {
            bonus.reset();
            float bonus = event.target.getStats().getInt(Stats.MAX_HEALTH) * BONUS_DAMAGE;
            float bleed = event.actor.getStats().get(Stats.DAMAGE) * BLEED_DAMAGE;
            event.bonus += bonus;
            event = event.actor.ability(event);
            event.output.add(event.target.buff(new DebuffBleed(event.actor, BLEED_TURNS, bleed)));
        }
        return event;
    }

    @Override
    public String onTurnStart(GameMember member) {
        if (crush.count() && crush.tryNotify())
            return Emote.INFO + "**" + member.getUsername() + "**'s Crush is ready to use.";
        return null;
    }

    public Stacker getBonus() {
        return bonus;
    }

    public Cooldown getCrush() {
        return crush;
    }

    public static class CrushCommand implements Command {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                Duelist unit = (Duelist) member.getUnit();
                message.delete().block();
                if (member.hasData(DebuffSilence.class))
                    Util.sendFailure(channel, "You cannot **Crush** while silenced.");
                else if (!unit.crush.isDone())
                    Util.sendFailure(channel, "**Crush** is on cooldown for **" + unit.crush.getCurrent() + "** more turn(s).");
                else
                    member.act(new CrushAction(game.getRandomTarget(member)));
            }
        }

        @Override
        public String getName() {
            return "crush";
        }

        @Override
        public String getDescription() {
            return "Weakens the target by **" + percent(CRUSH_POWER) + "** for **" + CRUSH_TURNS + "** turns." +
                    "\nDebuffs received by the target while weakened are extended by **" + CRUSH_EXTEND + "** turn.";
        }
    }

    public static class CrushAction implements GameAction {
        private final GameMember target;

        public CrushAction(GameMember target) {
            this.target = target;
        }

        @Override
        public String act(GameMember actor) {
            Duelist unit = (Duelist) actor.getUnit();
            unit.crush.start();
            return Util.joinNonEmpty("\n", Emote.USE + "**" + actor.getUsername() + "** used **Crush**!",
                    target.buff(new DebuffWeaken(actor, Duelist.CRUSH_TURNS, Duelist.CRUSH_POWER)));
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }
}
