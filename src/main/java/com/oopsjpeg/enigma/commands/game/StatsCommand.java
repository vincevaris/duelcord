package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class StatsCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        User author = message.getAuthor();
        MessageChannel channel = message.getChannel();
        Game game = Enigma.getPlayer(author).getGame();

        if (channel.equals(game.getChannel())) {
            message.delete().complete();
            if (game.getGameState() == 0)
                Util.sendError(channel, "You cannot check your gold until the game has started.");
            else
                channel.sendMessage(Emote.BUY + "**" + author.getName() + "** has **"
                        + game.getMember(author).getStats().getInt(Stats.GOLD) + "** gold.").complete();
        }
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"gold"};
    }
}
