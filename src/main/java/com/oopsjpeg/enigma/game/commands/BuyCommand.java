package com.oopsjpeg.enigma.game.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.util.List;

public class BuyCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        User author = message.getAuthor().orElse(null);
        MessageChannel channel = message.getChannel().block();
        Game game = Enigma.getInstance().getPlayer(author).getGame();
        Game.Member member = game.getMember(author);

        if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
            message.delete().block();
            if (game.getGameState() == 0)
                Util.sendFailure(channel, "You cannot buy items until the game has started.");
            else {
                Item item = Item.fromName(String.join(" ", args));
                if (item == null)
                    Util.sendFailure(channel, "Invalid item. Please try again.");
                else {
                    int cost = item.getCost();
                    List<Item> build = member.getItems();
                    for (Item i : item.getBuild())
                        if (build.contains(i)) {
                            cost -= i.getCost();
                            build.remove(i);
                        }

                    if (member.getStats().getInt(Stats.GOLD) < cost)
                        Util.sendFailure(channel, "You need **" + (cost - member.getStats().getInt(Stats.GOLD)) + "** more gold for a(n) **" + item.getName() + "**.");
                    else if (build.size() >= 4)
                        Util.sendFailure(channel, "You do not have enough inventory space for a(n) **" + item.getName() + "**.");
                    else
                        member.act(game.new BuyAction(item, cost));
                }
            }
        }
    }

    @Override
    public String getName() {
        return "buy";
    }
}
