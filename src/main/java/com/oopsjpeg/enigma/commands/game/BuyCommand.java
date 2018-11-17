package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.util.Util;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class BuyCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
		Game game = Enigma.getPlayer(author).getGame();
		Game.Member member = game.getMember(author);

		if (member.equals(game.getCurrentMember())) {
			Bufferer.deleteMessage(message);
			if (game.getGameState() == 0)
				Util.sendError(channel, "You cannot buy items until the game has started.");
			else {
				Item item = Item.fromName(String.join(" ", args));
				if (item == null)
					Util.sendError(channel, "Invalid item. Please try again.");
				else
					member.act(game.new BuyAction(item));
			}
		}
	}

	@Override
	public String getName() {
		return "buy";
	}
}
