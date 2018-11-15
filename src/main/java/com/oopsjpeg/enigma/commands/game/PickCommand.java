package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.commands.util.GameCommand;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.util.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import com.oopsjpeg.roboops.framework.Bufferer;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class PickCommand implements GameCommand {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		GameCommand.super.execute(message, alias, args);
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
		Game game = Enigma.getPlayer(author).getGame();
		Game.Member member = game.getMember(author);

		if (member.equals(game.getCurrentMember())) {
			if (game.getGameState() == 1)
				Util.sendError(channel, "You cannot pick a unit after the game has started.");
			else {
				Unit unit = Unit.fromName(String.join(" ", args));
				if (unit == null)
					Util.sendError(channel, "Invalid unit. Please try again.");
				else {
					member.setUnit(unit);
					Bufferer.sendMessage(channel, Emote.YES + "**" + author.getName() + "** has picked **"
							+ unit.getName() + "**.");
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
