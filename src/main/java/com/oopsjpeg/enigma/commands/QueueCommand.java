package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Emoji;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.Util;
import com.oopsjpeg.enigma.commands.util.Command;
import com.oopsjpeg.enigma.commands.util.CommandInput;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.storage.Player;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class QueueCommand implements Command {
    @Override
    public void execute(CommandInput input) {
        IMessage message = input.getMessage();
        IChannel channel = message.getChannel();
        IUser author = message.getAuthor();
        Player player = Enigma.getPlayer(author);

        GameMode mode = GameMode.DUEL;
        List<Player> queue = Enigma.getQueue(mode);

        Util.deleteMessage(message);
        if (player.getGame() == null) {
            if (!queue.contains(player)) {
                queue.add(player);
                player.setQueue(mode);
                Util.sendMessage(channel, Emoji.YES + author + " You are now in queue for **" + mode.getName()
                        + "** (size: **" + queue.size() + "**)");
            } else {
                queue.remove(player);
                player.clearQueue();
                Util.sendMessage(channel, Emoji.YES + author + " You are no longer in queue.");
            }
        }
    }

    @Override
    public String getName() {
        return "queue";
    }
}
