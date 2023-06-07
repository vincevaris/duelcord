package com.oopsjpeg.enigma.game.action;

import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Item;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

import java.util.ArrayList;
import java.util.List;

public class UseAction implements GameAction
{
    private final Item item;

    public UseAction(Item item)
    {
        this.item = item;
    }

    @Override
    public String act(GameMember actor)
    {
        final List<String> output = new ArrayList<>();
        output.add(Emote.SKILL + "**" + actor.getUsername() + "** used **" + item.getName() + "**.");
        output.add(item.onUse(actor));

        if (item.removeOnUse())
            actor.getItems().remove(item);

        return Util.joinNonEmpty("\n", output);
    }

    @Override
    public int getEnergy()
    {
        return 25;
    }
}