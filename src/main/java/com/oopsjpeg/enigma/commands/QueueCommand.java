package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

public class QueueCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel().block();
        User author = message.getAuthor().orElse(null);
        Player player = Enigma.getInstance().getPlayer(author);

        if (!channel.equals(Enigma.getInstance().getMatchmakingChannel()))
            Util.sendFailure(channel, "You must be in " + Enigma.getInstance().getMatchmakingChannel().getMention() + " to queue for games.");
        else if (player.getGame() != null)
            Util.sendFailure(channel, "You're already in a match.");
        else if (player.getQueue() != null) {
            player.removeQueue();
            Util.sendFailure(channel, "You have left the queue.");
        } else {
            GameMode mode = args.length > 0 ? GameMode.fromName(args[0]) : GameMode.DUEL;
            if (mode == null)
                Util.sendFailure(channel, "Invalid game mode.");
            else {
                player.setQueue(mode);
                Util.sendSuccess(channel, "**" + author.getUsername() + "** is in queue for **" + mode.getName() + "**.");
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
