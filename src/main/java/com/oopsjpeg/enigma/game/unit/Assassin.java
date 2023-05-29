package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.DebuffSilence;
import com.oopsjpeg.enigma.game.object.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

import static com.oopsjpeg.enigma.util.Util.percent;

public class Assassin extends Unit {
    public static final float POTENCY_STORE = 0.25f;
    public static final int POTENCY_TURNS = 3;
    public static final float SLASH_DAMAGE = 0.25f;
    public static final float SLASH_AP = 0.5f;
    public static final int SLASH_MAX = 4;
    public static final int SILENCE_TURNS = 1;

    private final Stacker slash = new Stacker(SLASH_MAX);
    private final Stacker potency = new Stacker(POTENCY_TURNS);
    private boolean slashed = false;
    private float potencyTotal = 0;

    public Assassin() {
        super("Assassin", new Command[]{new SlashCommand()}, Color.of(0, 69, 255), new Stats()
                .put(Stats.ENERGY, 125)
                .put(Stats.MAX_HEALTH, 720)
                .put(Stats.DAMAGE, 22)
                .put(Stats.HEALTH_PER_TURN, 9));
    }

    @Override
    public String onTurnEnd(GameMember member) {
        slashed = false;
        if (potency.stack())
            return Emote.KNIFE + "**" + member.getUsername() + "'s Potency** is at max capacity.";
        return null;
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        // Assassin potency stacking
        if (!potency.isDone())
            potencyTotal += event.damage * POTENCY_STORE;
        return event;
    }

    @Override
    public String getDescription() {
        return "**" + percent(POTENCY_STORE) + "** of the damage dealt last turn is stored as **Potency**." +
                "\nThis can occur **" + POTENCY_TURNS + "** times until **Potency** is consumed.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{"Slash: **" + slash.getCurrent() + " / " + Assassin.SLASH_MAX + "**", "Potency: **" + Math.round(potencyTotal) + "**"};
    }

    public Stacker getSlash() {
        return slash;
    }

    public Stacker getPotency() {
        return potency;
    }

    public boolean hasSlashed() {
        return slashed;
    }

    public void setSlashed(boolean slashed) {
        this.slashed = slashed;
    }

    public float getPotencyTotal() {
        return potencyTotal;
    }

    public void setPotencyTotal(float potencyTotal) {
        this.potencyTotal = potencyTotal;
    }

    public static class SlashCommand implements Command {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                Assassin unit = (Assassin) member.getUnit();
                if (member.hasData(DebuffSilence.class))
                    Util.sendFailure(channel, "You cannot **Slash** while silenced.");
                else if (unit.slashed)
                    Util.sendFailure(channel, "You can only use **Slash** once per turn.");
                else
                    member.act(new SlashAction(game.getRandomTarget(member)));
            }
        }

        @Override
        public String getName() {
            return "slash";
        }

        @Override
        public String getDescription() {
            return "Deals **" + percent(SLASH_DAMAGE) + "** (+" + percent(SLASH_AP) + " AP) damage." +
                    "\nEvery **" + SLASH_MAX + "th** use applies **Silence** for **" + SILENCE_TURNS + "** turns and consumes **Potency** to deal bonus damage equal to it." +
                    "\n**Slash** doesn't count towards **Potency**.";
        }
    }

    public static class SlashAction implements GameAction {
        private final GameMember target;

        public SlashAction(GameMember target) {
            this.target = target;
        }

        @Override
        public String act(GameMember actor) {
            Assassin unit = (Assassin) actor.getUnit();
            unit.setSlashed(true);

            DamageEvent event = new DamageEvent(actor.getGame(), actor, target);
            event.damage = (actor.getStats().get(Stats.DAMAGE) * Assassin.SLASH_DAMAGE) + (actor.getStats().get(Stats.ABILITY_POWER) * Assassin.SLASH_AP);

            if (unit.getSlash().stack()) {
                event.damage += unit.getPotencyTotal();
                event.output.add(target.buff(new DebuffSilence(actor, Assassin.SILENCE_TURNS)));
                unit.getSlash().reset();
                unit.getPotency().reset();
                unit.setPotencyTotal(0);
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
