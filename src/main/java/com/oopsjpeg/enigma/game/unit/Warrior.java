package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
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

public class Warrior extends Unit {
    public static final int BONUS_MAX = 3;
    public static final float BONUS_DAMAGE = 0.25f;
    public static final float BASH_DAMAGE = 0.25f;
    public static final float BASH_HP_SCALE = 0.3f;
    public static final int BASH_COOLDOWN = 2;

    private Stacker bonus = new Stacker(BONUS_MAX);
    private Cooldown bash = new Cooldown(BASH_COOLDOWN);

    public Stacker getBonus() {
        return bonus;
    }

    public Cooldown getBash() {
        return bash;
    }

    @Override
    public String getName() {
        return "Warrior";
    }

    @Override
    public String getDescription() {
        return "Every **" + BONUS_MAX + "rd** attack deals **" + Util.percent(BONUS_DAMAGE) + "** bonus damage."
                + "\n\nUsing `>bash` breaks the target's shield and resist then deals **" + Util.percent(BASH_DAMAGE) + "** base damage (+" + Util.percent(BASH_HP_SCALE) + " bonus max health)."
                + "\n**Bash** counts towards stacks of bonus damage, but does not proc it."
                + "\n**Bash** can only be used once every **" + BASH_COOLDOWN + "** turn(s).";
    }

    @Override
    public Command[] getCommands() {
        return new Command[]{new BashCommand()};
    }

    @Override
    public String[] getTopic() {
        return new String[]{"Attack: **" + getBonus().getCurrent() + " / 3**"};
    }

    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public Stats getStats() {
        return new Stats()
                .put(Stats.ENERGY, 125)
                .put(Stats.MAX_HEALTH, 795)
                .put(Stats.DAMAGE, 22)
                .put(Stats.HEALTH_PER_TURN, 12);
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

    public class BashCommand implements Command {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (member.hasData(Silence.class))
                    Util.sendFailure(channel, "You cannot **Bash** while silenced.");
                else if (!getBash().isDone())
                    Util.sendFailure(channel, "**Bash** is on cooldown for **" + getBash().getCurrent() + "** more turn(s).");
                else
                    member.act(new BashAction(game.getRandomTarget(member)));
            }
        }

        @Override
        public String[] getAliases() {
            return new String[]{"bash"};
        }
    }

    public class BashAction implements GameAction {
        private final GameMember target;

        public BashAction(GameMember target) {
            this.target = target;
        }

        @Override
        public String act(GameMember actor) {
            getBash().start();
            getBonus().stack();

            DamageEvent event = new DamageEvent(actor.getGame(), actor, target);

            event.target.setDefensive(false);
            event.target.getStats().put(Stats.RESIST, 0);
            event.damage = (actor.getStats().get(Stats.DAMAGE) * Warrior.BASH_DAMAGE);
            event.bonus = (actor.getStats().get(Stats.MAX_HEALTH) - actor.getUnit().getStats().get(Stats.MAX_HEALTH)) * Warrior.BASH_HP_SCALE;
            if (event.target.getStats().get(Stats.SHIELD) > 0)
                event.target.getStats().put(Stats.SHIELD, 0.01f);

            event = event.actor.ability(event);

            return actor.damage(event, Emote.KNIFE, "Bash");
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }
}
