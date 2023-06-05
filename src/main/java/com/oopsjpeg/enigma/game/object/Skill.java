package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.GameMemberVars;
import com.oopsjpeg.enigma.game.buff.SilenceDebuff;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

public abstract class Skill implements Command
{
    private final Unit unit;
    private final int baseCooldown;

    public Skill(Unit unit, int baseCooldown)
    {
        this.unit = unit;
        this.baseCooldown = baseCooldown;
    }

    @Override
    public void execute(Message message, String[] args)
    {
        MessageChannel channel = message.getChannel().block();
        GameMember actor = Enigma.getGameMemberFromMessage(message);
        Game game = actor.getGame();
        GameMemberVars vars = actor.getVars();
        Cooldown cooldown = getCooldown(vars);

        if (!channel.equals(game.getChannel()) || !actor.equals(game.getCurrentMember()))
            return;

        message.delete().subscribe();

        if (actor.hasBuff(SilenceDebuff.class))
        {
            Util.sendFailure(channel, "You can't use skills while silenced.");
            return;
        }

        if (!cooldown.isDone())
        {
            Util.sendFailure(channel, "This skill is on cooldown.");
            return;
        }

        actor.act(act(game, actor));
    }

    public abstract GameAction act(Game game, GameMember actor);

    public String getCooldownVar()
    {
        return getName() + "_cooldown";
    }

    public Cooldown getCooldown(GameMemberVars vars)
    {
        if (!vars.has(unit, getCooldownVar()))
            setCooldown(vars, new Cooldown(baseCooldown));
        return vars.get(unit, getCooldownVar(), Cooldown.class);
    }

    public void setCooldown(GameMemberVars vars, Cooldown cooldown)
    {
        vars.put(unit, getCooldownVar(), cooldown);
    }

    public boolean hasCooldown()
    {
        return baseCooldown > 0;
    }

    public Unit getUnit()
    {
        return unit;
    }

    public int getBaseCooldown()
    {
        return baseCooldown;
    }
}
