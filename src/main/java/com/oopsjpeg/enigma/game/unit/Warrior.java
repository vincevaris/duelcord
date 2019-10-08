package com.oopsjpeg.enigma.game.unit;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.*;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.awt.*;

public class Warrior extends Unit {
    public static final int BONUS_MAX = 3;
    public static final float BONUS_DAMAGE = 0.3f;
    public static final float BASH_DAMAGE = 0.5f;
    public static final float BASH_HP_SCALE = 0.25f;
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
                + "\n**Bash** counts towards stacks of bonus damages, but does not proc it."
                + "\n**Bash** can only be used once every **" + BASH_COOLDOWN + "** turn(s).";
    }

    @Override
    public Command[] getCommands() {
        return new Command[]{new BashCommand()};
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
                .put(Stats.DAMAGE, 23);
    }

    @Override
    public Stats getPerTurn() {
        return new Stats()
                .put(Stats.HEALTH, 13);
    }

    @Override
    public String onTurnStart(Game.Member member) {
        if (bash.count() && bash.notif())
            return Emote.INFO + "**" + member.getUsername() + "'s Bash** is ready to use.";
        return "";
    }

    @Override
    public DamageEvent onBasicAttack(DamageEvent event) {
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
            Game.Member member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (game.getGameState() == 0)
                    Util.sendFailure(channel, "You cannot use **Bash** until the game has started.");
                else {
                    Game.Member target = game.getAlive().stream().filter(m -> !m.equals(member)).findAny().orElse(null);
                    if (target == null)
                        Util.sendFailure(channel, "There is no one to use **Bash** on.");
                    else
                        member.act(game.new BashAction(target));
                }
            }
        }

        @Override
        public String getName() {
            return "bash";
        }
    }

}
