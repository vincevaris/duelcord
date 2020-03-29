package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.awt.*;

public class Assassin extends Unit {
    public static final float POTENCY_STORE = 0.25f;
    public static final int POTENCY_TURNS = 5;
    public static final float SLASH_DAMAGE = 0.25f;
    public static final float SLASH_AP = 0.5f;
    public static final int SLASH_MAX = 3;
    public static final int SILENCE_TURNS = 1;

    private boolean slashed = false;
    private final Stacker slash = new Stacker(SLASH_MAX);
    private final Stacker potency = new Stacker(POTENCY_TURNS);
    private float potencyTotal = 0;

    public boolean getSlashed() {
        return slashed;
    }

    public void setSlashed(boolean slashed) {
        this.slashed = slashed;
    }

    public Stacker getSlash() {
        return slash;
    }

    public Stacker getPotency() {
        return potency;
    }

    public float getPotencyTotal() {
        return potencyTotal;
    }

    public void setPotencyTotal(float potencyTotal) {
        this.potencyTotal = potencyTotal;
    }

    @Override
    public String onTurnEnd(Game.Member member) {
        slashed = false;
        if (potency.stack())
            return Emote.KNIFE + "**" + member.getUsername() + "'s Potency** is at max capacity.";
        return "";
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        // Assassin potency stacking
        if (!potency.done())
            potencyTotal += event.damage * POTENCY_STORE;
        return event;
    }

    @Override
    public String getName() {
        return "Assassin";
    }

    @Override
    public String getDescription() {
        return "**" + Util.percent(POTENCY_STORE) + "**"
                + " of damage dealt in the last turn is stored as **Potency**."
                + " This can only occur **" + POTENCY_TURNS + "** times until **Potency** is reset."
                + "\n\nUsing `>slash` deals **" + Util.percent(SLASH_DAMAGE) + "** base damage (+" + Util.percent(SLASH_AP) + " AP)."
                + " Every **" + SLASH_MAX + "rd** slash applies **Silence** for **" + SILENCE_TURNS + "** turn(s) and deals"
                + " bonus damage equal to the total **Potency**, resetting it as well."
                + "\n\nSlash does not count towards total **Potency**.";
    }

    @Override
    public Command[] getCommands() {
        return new Command[]{new SlashCommand()};
    }

    @Override
    public String[] getTopic() {
        return new String[]{"Slash: **" + getSlash().getCur() + " / " + Assassin.SLASH_MAX + "**",
                "Potency: **" + Math.round(getPotencyTotal()) + "**"};
    }

    @Override
    public Color getColor() {
        return new Color(0, 69, 255);
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.ENERGY, 125)
                .put(Stats.MAX_HEALTH, 720)
                .put(Stats.DAMAGE, 24)
                .put(Stats.HEALTH_PER_TURN, 11);
    }

    public class SlashCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            Game.Member member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (member.hasData(Silence.class))
                    Util.sendFailure(channel, "You cannot **Slash** while silenced.");
                else if (getSlashed())
                    Util.sendFailure(channel, "You can only use **Slash** once per turn.");
                else
                   member.act(new SlashAction(game.getRandomTarget(member)));
            }
        }

        @Override
        public String[] getAliases() {
            return new String[]{"slash"};
        }
    }

    public class SlashAction implements GameAction {
        private final Game.Member target;

        public SlashAction(Game.Member target) {
            this.target = target;
        }

        @Override
        public String act(Game.Member actor) {
            setSlashed(true);

            DamageEvent event = new DamageEvent(actor.getGame(), actor, target);
            event.damage = (actor.getStats().get(Stats.DAMAGE) * Assassin.SLASH_DAMAGE) + (actor.getStats().get(Stats.ABILITY_POWER) * Assassin.SLASH_AP);

            if (getSlash().stack()) {
                event.damage += getPotencyTotal();
                event.output.add(target.buff(new Silence(actor, Assassin.SILENCE_TURNS)));
                setSlashed(false);
                getSlash().reset();
                getPotency().reset();
                setPotencyTotal(0);
            }

            event = event.actor.hit(event);
            event = event.actor.crit(event);
            event = event.actor.ability(event);

            return actor.damage(event, Emote.KNIFE, "Slash");
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }
}
