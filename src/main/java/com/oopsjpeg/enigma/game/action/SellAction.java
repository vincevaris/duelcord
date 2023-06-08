package com.oopsjpeg.enigma.game.action;

import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Item;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

import java.util.ArrayList;
import java.util.List;

public class SellAction implements GameAction
{
    private final Item item;

    public SellAction(Item item)
    {
        this.item = item;
    }

    @Override
    public String act(GameMember actor)
    {
        final List<String> output = new ArrayList<>();
        int gold = Math.round(item.getCost() * 0.6f);
        output.add(Emote.GOLD + "**" + actor.getUsername() + "** sold **" + item.getName() + "** for __" + gold + "__ gold.");
        actor.giveGold(gold);
        actor.getItems().remove(item);
        output.add(actor.updateStats());
        return Util.joinNonEmpty("\n", output);
    }

    @Override
    public int getEnergy()
    {
        return 0;
    }
}