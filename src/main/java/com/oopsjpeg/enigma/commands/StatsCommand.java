package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

public class StatsCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel().block();
        User author = message.getAuthor().orElse(null);
        Player player = Enigma.getInstance().getPlayer(author);

        if (player == null)
            Util.sendFailure(channel, "You do not have any stats.");
        else {
            Util.send(channel, author.getUsername() + "'s Stats", "**" + player.getWins() + "**W **"
                    + player.getLosses() + "**L (**" + Util.percent(player.getWinRate()) + "** WR)"
                    + "\nGems: **" + player.getGems() + "**");
        }
    }

    @Override
    public String getName() {
        return "stats";
    }
}
