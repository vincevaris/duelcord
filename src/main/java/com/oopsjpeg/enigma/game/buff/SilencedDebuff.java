package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.util.Emote;

public class SilencedDebuff extends Buff
{
    public SilencedDebuff(GameMember source)
    {
        super("Silenced", true, source, 1, 0);
    }

    @Override
    public String onTurnStart(GameMember member)
    {
        return Emote.SILENCE + "**" + member.getUsername() + "** can't use skills or defend this turn.";
    }

    @Override
    public String getStatus(GameMember member)
    {
        return "Silenced: Can't use Skills or Defend";
    }
}
