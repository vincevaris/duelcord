package com.oopsjpeg.enigma.game.action;

import com.oopsjpeg.enigma.game.Build;
import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.object.Item;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

import java.util.ArrayList;
import java.util.List;

public class BuyAction implements GameAction {
    private final Build build;

    public BuyAction(Build build) {
        this.build = build;
    }

    @Override
    public String act(GameMember actor) {
        List<String> output = new ArrayList<>();
        Item item = build.getItem();

        actor.takeGold(build.getCost());
        actor.getItems().clear();
        actor.getItems().add(item);
        actor.getItems().addAll(build.getPostData());
        actor.updateStats();

        output.add(0, Emote.BUY + "**" + actor.getUsername() + "** purchased a(n) **"
                + item.getName() + "** for **" + build.getCost() + "** gold.");

        return Util.joinNonEmpty("\n", output);
    }

    @Override
    public int getEnergy() {
        return 25;
    }
}