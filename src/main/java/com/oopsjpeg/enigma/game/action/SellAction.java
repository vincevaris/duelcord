package com.oopsjpeg.enigma.game.action;

import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.util.Emote;

import static com.oopsjpeg.enigma.game.Stats.GOLD;

public class SellAction implements GameAction {
    private final Item item;

    public SellAction(Item item) {
        this.item = item;
    }

    @Override
    public String act(GameMember actor) {
        int gold = Math.round(item.getCost() * 0.6f);
        String output = Emote.BUY + "**" + actor.getUsername() + "** sold a(n) **" + item.getName() + "** for **" + gold + "** gold.";
        actor.getStats().add(GOLD, gold);
        actor.getData().remove(item);
        actor.updateStats();
        return output;
    }

    @Override
    public int getEnergy() {
        return 25;
    }
}