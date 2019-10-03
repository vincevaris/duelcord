package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

public class BashCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        User author = message.getAuthor().orElse(null);
        MessageChannel channel = message.getChannel().block();
        Game game = Enigma.getInstance().getPlayer(author).getGame();
        Game.Member member = game.getMember(author);

        if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
            message.delete().block();
            if (game.getGameState() == 0)
                Util.sendFailure(channel, "You cannot use **Bash** until the game has started.");
            else {
                Game.Member target = game.getAlive().stream().filter(m -> !m.equals(member)).findAny().orElse(null);
                if (target == null)
                    Util.sendFailure(channel, "There is no one to use **Bash** on.");
                else
                    member.act(game.new BashAction(target));
            }
        }
    }

    @Override
    public String getName() {
        return "bash";
    }
}
