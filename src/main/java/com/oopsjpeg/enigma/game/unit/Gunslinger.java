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
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Gunslinger extends Unit {
    public static final float BONUS_DAMAGE = 0.25f;
    public static final float BONUS_AP = 1.2f;
    public static final int BARRAGE_SHOTS = 4;
    public static final int BARRAGE_DAMAGE = 6;
    public static final float BARRAGE_AP = 0.3f;
    public static final int BARRAGE_COOLDOWN = 3;

    private boolean bonus = false;
    private final Cooldown barrage = new Cooldown(BARRAGE_COOLDOWN);

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
            event.bonus += (event.damage * BONUS_DAMAGE) + (event.actor.getStats().get(Stats.ABILITY_POWER) * BONUS_AP);
        }
        return event;
    }

    @Override
    public String onTurnStart(Game.Member member) {
        setBonus(false);
        if (barrage.count() && barrage.notif())
            return Emote.INFO + "**" + member.getUsername() + "'s Barrage** is ready to use.";
        return "";
    }

    @Override
    public String getName() {
        return "Gunslinger";
    }

    @Override
    public String getDescription() {
        return "The first basic attack per turn always crits and deals **"
                + Util.percent(BONUS_DAMAGE) + "** (+" + Util.percent(BONUS_AP) + " AP) bonus damage.\n\n"
                + "Using `>barrage` fires **" + BARRAGE_SHOTS + "** shots that each deal **"
                + BARRAGE_DAMAGE + "** (+" + Util.percent(BARRAGE_AP) + " AP) damage.\n"
                + "Barrage shots can crit and apply on-hit effects.\n"
                + "Barrage can only be used once every **" + BARRAGE_COOLDOWN + "** turn(s).";
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
                .put(Stats.DAMAGE, 17)
                .put(Stats.HEALTH_PER_TURN, 12);
    }

    public class BarrageCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            Game.Member member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (member.hasData(Silence.class))
                    Util.sendFailure(channel, "You cannot **Barrage** while silenced.");
                else if (!getBarrage().done())
                    Util.sendFailure(channel, "**Barrage** is on cooldown for **" + getBarrage().getCur() + "** more turn(s).");
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
        private final Game.Member target;

        public BarrageAction(Game.Member target) {
            this.target = target;
        }

        @Override
        public String act(Game.Member actor) {
            getBarrage().start();

            List<String> output = new ArrayList<>();
            for (int i = 0; i < Gunslinger.BARRAGE_SHOTS; i++)
                if (target.isAlive()) {
                    DamageEvent event = new DamageEvent(actor.getGame(), actor, target);
                    event.damage = Gunslinger.BARRAGE_DAMAGE + (actor.getStats().get(Stats.ABILITY_POWER) * Gunslinger.BARRAGE_AP);
                    actor.crit(event);
                    actor.hit(event);
                    output.add(actor.damage(event, Emote.GUN, "Barrage"));
                }
            output.add(0, Emote.USE + "**" + actor.getUsername() + "** used **Barrage**!");

            return Util.joinNonEmpty(output);
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }
}
