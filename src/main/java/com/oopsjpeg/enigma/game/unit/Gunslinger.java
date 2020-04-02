package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Gunslinger extends Unit {
    public static final float BONUS_AP = 1.25f;
    public static final int BARRAGE_SHOTS = 4;
    public static final int BARRAGE_DAMAGE = 5;
    public static final float BARRAGE_AD_RATIO = 0.15f;
    public static final float BARRAGE_AP_RATIO = 0.25f;
    public static final int BARRAGE_COOLDOWN = 3;
    private final Cooldown barrage = new Cooldown(BARRAGE_COOLDOWN);
    private boolean bonus = false;

    public boolean getBonus() {
        return bonus;
    }

    public void setBonus(boolean bonus) {
        this.bonus = bonus;
    }

    public Cooldown getBarrage() {
        return barrage;
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        if (!getBonus()) {
            setBonus(true);
            event.crit = true;
            event.bonus += event.actor.getStats().get(Stats.ABILITY_POWER) * BONUS_AP;
        }
        return event;
    }

    @Override
    public String onTurnStart(GameMember member) {
        setBonus(false);
        if (barrage.count() && barrage.tryNotify())
            return Emote.INFO + "**" + member.getUsername() + "**'s Barrage is ready to use.";
        return null;
    }

    @Override
    public String getName() {
        return "Gunslinger";
    }

    @Override
    public String getDescription() {
        return "The first basic attack per turn always crits and deals **" + Util.percent(BONUS_AP) + " AP** bonus damage.\n\n"
                + "Using `>barrage` fires **" + BARRAGE_SHOTS + "** shots that each deal **"
                + BARRAGE_DAMAGE + "** (+" + Util.percent(BARRAGE_AD_RATIO) + " bonus AD) (+" + Util.percent(BARRAGE_AP_RATIO) + " AP) damage.\n"
                + "Barrage shots can crit and apply on-hit effects.\n"
                + "Barrage can only be used once every **" + BARRAGE_COOLDOWN + "** turn(s).";
    }

    @Override
    public String[] getTopic() {
        return new String[]{(barrage.isDone() ? "Barrage is ready." : "Barrage in **" + barrage.getCurrent() + "** turn(s).")};
    }

    @Override
    public Command[] getCommands() {
        return new Command[]{new BarrageCommand()};
    }

    @Override
    public Color getColor() {
        return new Color(255, 110, 0);
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.ENERGY, 125)
                .put(Stats.MAX_HEALTH, 750)
                .put(Stats.DAMAGE, 19)
                .put(Stats.HEALTH_PER_TURN, 12);
    }

    public class BarrageCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (member.hasData(Silence.class))
                    Util.sendFailure(channel, "You cannot **Barrage** while silenced.");
                else if (!getBarrage().isDone())
                    Util.sendFailure(channel, "**Barrage** is on cooldown for **" + getBarrage().getCurrent() + "** more turn(s).");
                else
                    member.act(new BarrageAction(game.getRandomTarget(member)));
            }
        }

        @Override
        public String[] getAliases() {
            return new String[]{"barrage"};
        }
    }

    public class BarrageAction implements GameAction {
        private final GameMember target;

        public BarrageAction(GameMember target) {
            this.target = target;
        }

        @Override
        public String act(GameMember actor) {
            getBarrage().start();

            List<String> output = new ArrayList<>();
            for (int i = 0; i < Gunslinger.BARRAGE_SHOTS; i++)
                if (target.isAlive()) {
                    DamageEvent event = new DamageEvent(actor.getGame(), actor, target);
                    event.damage = Gunslinger.BARRAGE_DAMAGE + (actor.getBonusDamage() * Gunslinger.BARRAGE_AD_RATIO) + (actor.getStats().get(Stats.ABILITY_POWER) * Gunslinger.BARRAGE_AP_RATIO);
                    actor.crit(event);
                    actor.hit(event);
                    output.add(actor.damage(event, Emote.GUN, "Barrage"));
                }
            output.add(0, Emote.USE + "**" + actor.getUsername() + "** used **Barrage**!");

            return Util.joinNonEmpty("\n", output);
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }
}
