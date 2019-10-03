package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.util.List;

public class QueueCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel().block();
        User author = message.getAuthor().orElse(null);
        Player player = Enigma.getInstance().getPlayer(author);

        GameMode mode = GameMode.DUEL;
        List<Player> queue = Enigma.getInstance().getQueue(mode);

        if (player.getGame() != null)
            Util.sendFailure(channel, "You're already in a match.");
        else {
            if (!queue.contains(player)) {
                player.setQueue(mode);
                Util.sendSuccess(channel, "**" + author.getUsername() + "** is in queue for **" + mode.getName() + "**.");
            } else {
                player.removeQueue();
                Util.sendFailure(channel, "You have left the queue.");
            }
        }
    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"find", "mm", "q"};
    }
}
