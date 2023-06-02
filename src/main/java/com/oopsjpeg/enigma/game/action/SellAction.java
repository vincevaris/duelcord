package com.oopsjpeg.enigma.game.action;

import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Item;
import com.oopsjpeg.enigma.util.Emote;

public class SellAction implements GameAction {
    private final Item item;

    public SellAction(Item item) {
        this.item = item;
    }

    @Override
    public String act(GameMember actor) {
        int gold = Math.round(item.getCost() * 0.6f);
        String output = Emote.BUY + "**" + actor.getUsername() + "** sold a(n) **" + item.getName() + "** for **" + gold + "** gold.";
        actor.giveGold(gold);
        actor.getItems().remove(item);
        actor.updateStats();
        return output;
    }

    @Override
    public int getEnergy() {
        return 25;
    }
}