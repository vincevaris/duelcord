package com.oopsjpeg.enigma.game.action;

import com.oopsjpeg.enigma.game.Build;
import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

import java.util.ArrayList;
import java.util.List;

import static com.oopsjpeg.enigma.game.Stats.GOLD;
import static com.oopsjpeg.enigma.game.Stats.MAX_HEALTH;

public class BuyAction implements GameAction {
    private final Build build;

    public BuyAction(Build build) {
        this.build = build;
    }

    @Override
    public String act(GameMember actor) {
        List<String> output = new ArrayList<>();
        Item item = build.getItem();

        actor.getStats().sub(GOLD, build.getCost());
        actor.getData().removeIf(o -> o instanceof Item);
        actor.getData().add(item);
        actor.getData().addAll(build.getPostData());
        actor.updateStats();

        if (item.getStats().get(MAX_HEALTH) > 0 && !actor.getItemHeals().contains(item)) {
            output.add(actor.heal(item.getStats().get(MAX_HEALTH) / 2, item.getName()));
            actor.getItemHeals().add(item);
        }

        output.add(0, Emote.BUY + "**" + actor.getUsername() + "** purchased a(n) **"
                + item.getName() + "** for **" + build.getCost() + "** gold.");

        return Util.joinNonEmpty("\n", output);
    }

    @Override
    public int getEnergy() {
        return 25;
    }
}