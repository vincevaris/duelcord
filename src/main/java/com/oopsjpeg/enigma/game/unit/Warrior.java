package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.DebuffSilence;
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

public class Warrior extends Unit {
    public static final int BONUS_MAX = 3;
    public static final float BONUS_DAMAGE = 0.25f;
    public static final float BASH_DAMAGE = 0.25f;
    public static final float BASH_HP_SCALE = 0.3f;
    public static final int BASH_COOLDOWN = 2;

    private final Stacker bonus = new Stacker(BONUS_MAX);
    private final Cooldown bash = new Cooldown(BASH_COOLDOWN);

    public Warrior() {
        super("Warrior", new Command[]{new BashCommand()}, Color.CYAN, new Stats()
                .put(Stats.MAX_ENERGY, 125)
                .put(Stats.MAX_HEALTH, 775)
                .put(Stats.DAMAGE, 22)
                .put(Stats.HEALTH_PER_TURN, 12));
    }

    @Override
    public String getDescription() {
        return "Every **" + BONUS_MAX + "rd** attack deals **" + percent(BONUS_DAMAGE) + "** bonus damage.";
    }

    @Override
    public String[] getTopic(GameMember member) {
        return new String[]{"Bonus: **" + bonus.getCurrent() + " / 3**",
                bash.isDone() ? "Bash is ready" : "Bash in **" + bash.getCurrent() + "** turn(s)"};
    }

    @Override
    public String onTurnStart(GameMember member) {
        if (bash.count() && bash.tryNotify())
            return Emote.INFO + "**" + member.getUsername() + "**'s Bash is ready to use.";
        return null;
    }

    @Override
    public DamageEvent basicAttackOut(DamageEvent event) {
        if (bonus.stack()) {
            event.bonus += event.actor.getStats().get(Stats.DAMAGE) * BONUS_DAMAGE;
            bonus.reset();
        }
        return event;
    }

    public static class BashCommand implements Command {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                Warrior unit = (Warrior) member.getUnit();
                message.delete().block();
                if (member.hasData(DebuffSilence.class))
                    Util.sendFailure(channel, "You cannot **Bash** while silenced.");
                else if (!unit.bash.isDone())
                    Util.sendFailure(channel, "**Bash** is on cooldown for **" + unit.bash.getCurrent() + "** more turn(s).");
                else
                    member.act(new BashAction(game.getRandomTarget(member)));
            }
        }

        @Override
        public String getName() {
            return "bash";
        }

        @Override
        public String getDescription() {
            return "Break the target's shield and resist, then deal **" + percent(BASH_DAMAGE) + "** (+" + percent(BASH_HP_SCALE) + " bonus max health) damage." +
                    "\n**Bash** stacks Warrior's passive, but doesn't activate it.";
        }
    }

    public static class BashAction implements GameAction {
        private final GameMember target;

        public BashAction(GameMember target) {
            this.target = target;
        }

        @Override
        public String act(GameMember actor) {
            Warrior unit = (Warrior) actor.getUnit();
            unit.bash.start();
            unit.bonus.stack();

            DamageEvent event = new DamageEvent(actor.getGame(), actor, target);

            event.target.setDefensive(false);
            if (event.target.getStats().get(Stats.RESIST) > 0) {
                event.target.getStats().put(Stats.RESIST, 0);
                event.output.add(Emote.SHIELD + " It broke their resist!");
            }
            event.damage = (actor.getStats().get(Stats.DAMAGE) * Warrior.BASH_DAMAGE);
            event.bonus = (actor.getStats().get(Stats.MAX_HEALTH) - actor.getUnit().getStats().get(Stats.MAX_HEALTH)) * Warrior.BASH_HP_SCALE;
            if (event.target.hasShield()) {
                event.target.setShield(0);
                event.output.add(Emote.SHIELD + " It broke their shield!");
            }

            event = event.actor.ability(event);

            return actor.damage(event, Emote.KNIFE, "Bash");
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }
}
