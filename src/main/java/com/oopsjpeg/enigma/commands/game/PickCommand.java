package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class PickCommand implements Command {
	@Override
	public void execute(Message message, String alias, String[] args) {
		User author = message.getAuthor();
		MessageChannel channel = message.getChannel();
		Game game = Enigma.getPlayer(author).getGame();
		Game.Member member = game.getMember(author);

		if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
			message.delete().complete();
			if (game.getGameState() == 1)
				Util.sendError(channel, "You cannot pick a unit after the game has started.");
			else {
				Unit unit = Unit.fromName(String.join(" ", args));
				if (unit == null)
					Util.sendError(channel, "Invalid unit. Please try again.");
				else {
					member.setUnit(unit);
					channel.sendMessage(Emote.YES + "**" + author.getName() + "** has picked **"
							+ unit.getName() + "**.").complete();
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
