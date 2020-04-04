package com.oopsjpeg.enigma.game.action;

import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Item;
import com.oopsjpeg.enigma.util.Emote;

public class UseAction implements GameAction {
    private final Item item;

    public UseAction(Item item) {
        this.item = item;
    }

    @Override
    public String act(GameMember actor) {
        String output = Emote.USE + "**" + actor.getUsername() + "** used a(n) **" + item.getName() + "**.\n" + item.onUse(actor);
        if (item.removeOnUse()) actor.getData().remove(item);
        actor.updateStats();
        return output;
    }

    @Override
    public int getEnergy() {
        return 25;
    }
}