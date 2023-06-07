package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.SilencedDebuff;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

public abstract class Skill implements Command
{
    private final Unit unit;
    private final int baseCooldown;
    private final int energyCost;

    public Skill(Unit unit, int baseCooldown, int energyCost)
    {
        this.unit = unit;
        this.baseCooldown = baseCooldown;
        this.energyCost = energyCost;
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

        if (actor.hasBuff(SilencedDebuff.class))
        {
            Util.sendFailure(channel, "You can't use skills while silenced.");
            return;
        }

        if (hasCooldown() && !cooldown.isDone())
        {
            Util.sendFailure(channel, "**`>" + getName() + "`** will be ready in **" + cooldown.getCurrent() + "** turns.");
            return;
        }

        if (hasEnergyCost() && actor.getEnergy() < getEnergyCost())
        {
            Util.sendFailure(channel, "**`>" + getName() + "`** costs **" + energyCost + "** energy. You have **" + actor.getEnergy() + "**.");
            return;
        }

        actor.act(act(game, actor));
        cooldown.start(actor.getStats().getInt(Stats.COOLDOWN_REDUCTION));
        setCooldown(vars, cooldown);
    }

    public String getStatus(GameMember member)
    {
        Cooldown cooldown = getCooldown(member.getVars());
        return getName() + ": " + (cooldown.isDone() ? "Ready" : "in " + cooldown.getCurrent() + " turn" + (cooldown.getCurrent() > 1 ? "s" : ""));
    }

    public abstract GameAction act(Game game, GameMember actor);

    public Unit getUnit()
    {
        return unit;
    }

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

    public int getBaseCooldown()
    {
        return baseCooldown;
    }

    public int getEnergyCost()
    {
        return energyCost;
    }

    public boolean hasEnergyCost() {
        return energyCost > 0;
    }
}
