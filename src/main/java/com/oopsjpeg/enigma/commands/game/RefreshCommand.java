package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.util.Command;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class RefreshCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        User author = message.getAuthor();
        Game game = Enigma.getPlayer(author).getGame();

        if (message.getChannel().equals(game.getChannel())) {
            message.delete().complete();
            game.setTopic(game.getMember(author));
        }
    }

    @Override
    public String getName() {
        return "refresh";
    }
}
