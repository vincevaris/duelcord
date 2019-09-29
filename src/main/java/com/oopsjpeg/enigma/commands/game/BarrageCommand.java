package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class BarrageCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        User author = message.getAuthor();
        MessageChannel channel = message.getChannel();
        Game game = Enigma.getPlayer(author).getGame();
        Game.Member member = game.getMember(author);

        if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
            message.delete().complete();
            if (game.getGameState() == 0)
                Util.sendError(channel, "You cannot use **Barrage** until the game has started.");
            else {
                Game.Member target = game.getAlive().stream().filter(m -> !m.equals(member)).findAny().orElse(null);
                if (target == null)
                    Util.sendError(channel, "There is no one to use **Barrage** on.");
                else
                    member.act(game.new BarrageAction(target));
            }
        }
    }

    @Override
    public String getName() {
        return "barrage";
    }
}
