package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.buff.Bleed;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.game.buff.Weaken;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.*;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.awt.*;

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

    public Stacker getBonus() {
        return bonus;
    }

    public Cooldown getCrush() {
        return crush;
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
        if (bonus.stack()) {
            bonus.reset();
            float bonus = event.target.getStats().getInt(Stats.MAX_HEALTH) * BONUS_DAMAGE;
            float bleed = event.actor.getStats().get(Stats.DAMAGE) * BLEED_DAMAGE;
            event.bonus += bonus;
            event.output.add(event.target.buff(new Bleed(event.actor, BLEED_TURNS, bleed)));
        }
        return event;
    }

    @Override
    public String onTurnStart(Game.Member member) {
        if (crush.count() && crush.notif())
            return Emote.INFO + "**" + member.getUsername() + "'s Crush** is ready to use.";
        return "";
    }

    @Override
    public String getName() {
        return "Duelist";
    }

    @Override
    public String getDescription() {
        return "Every **" + BONUS_MAX + "th** basic attack deals bonus damage equal to **"
                + Util.percent(BONUS_DAMAGE) + "** of the target's max health and applies **Bleed** for **"
                + Util.percent(BLEED_DAMAGE) + "** base damage for **" + BLEED_TURNS + "** turn(s).\n\n"
                + "Using `>crush` weakens the target by **" + Util.percent(CRUSH_POWER) + "** for **" + CRUSH_TURNS + "** turn(s).\n"
                + "If the target receives any other debuff while weakened, it is extended by **" + CRUSH_EXTEND + "** turn(s).\n"
                + "Crush can only be used once every **" + CRUSH_COOLDOWN + "** turn(s).";
    }

    @Override
    public Command[] getCommands() {
        return new Command[]{new CrushCommand()};
    }

    @Override
    public String[] getTopic() {
        return new String[]{"Bonus: **" + getBonus().getCur() + " / " + Duelist.BONUS_MAX + "**"};
    }

    @Override
    public Color getColor() {
        return Color.MAGENTA;
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.ENERGY, 125)
                .put(Stats.MAX_HEALTH, 750)
                .put(Stats.DAMAGE, 21)
                .put(Stats.HEALTH_PER_TURN, 10);
    }

    public class CrushCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            Game.Member member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (member.hasData(Silence.class))
                    Util.sendFailure(channel, "You cannot **Crush** while silenced.");
                else if (!getCrush().done())
                    Util.sendFailure(channel, "**Crush** is on cooldown for **" + getCrush().getCur() + "** more turn(s).");
                else
                    member.act(new CrushAction(game.getRandomTarget(member)));
            }
        }

        @Override
        public String getName() {
            return "crush";
        }
    }

    public class CrushAction implements GameAction {
        private final Game.Member target;

        public CrushAction(Game.Member target) {
            this.target = target;
        }

        @Override
        public String act(Game.Member actor) {
            getCrush().start();
            return Util.joinNonEmpty(Emote.USE + "**" + actor.getUsername() + "** used **Crush**!**",
                    target.buff(new Weaken(actor, Duelist.CRUSH_TURNS, Duelist.CRUSH_POWER)));
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }
}
