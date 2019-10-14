package com.oopsjpeg.enigma.game.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.util.Command;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

public class RefreshCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        User author = message.getAuthor().orElse(null);
        MessageChannel channel = message.getChannel().block();
        Game game = Enigma.getInstance().getPlayer(author).getGame();

        if (channel.equals(game.getChannel())) {
            message.delete().block();
            game.setTopic(game.getMember(author));
        }
    }

    @Override
    public String getName() {
        return "refresh";
    }
}
