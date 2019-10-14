package com.oopsjpeg.enigma.game.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

public class PickCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        User author = message.getAuthor().orElse(null);
        MessageChannel channel = message.getChannel().block();
        Game game = Enigma.getInstance().getPlayer(author).getGame();
        Game.Member member = game.getMember(author);

        if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
            message.delete().block();
            if (game.getGameState() == 1)
                Util.sendFailure(channel, "You cannot pick a unit after the game has started.");
            else {
                String name = String.join(" ", args);
                Unit unit = name.equalsIgnoreCase("random") ? Unit.values()[Util.RANDOM.nextInt(Unit.values().length)]
                        : Unit.fromName(String.join(" ", args));
                if (unit == null)
                    Util.sendFailure(channel, "Invalid unit.");
                else {
                    member.setUnit(unit);
                    channel.createMessage(Emote.YES + "**" + author.getUsername() + "** has picked **" + unit.getName() + "**.").block();
                    game.nextTurn();
                }
            }
        }
    }

    @Override
    public String getName() {
        return "pick";
    }
}
