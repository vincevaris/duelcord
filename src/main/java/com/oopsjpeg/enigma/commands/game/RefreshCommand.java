package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class RefreshCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IUser author = message.getAuthor();
		Game game = Enigma.getPlayer(author).getGame();

		if (message.getChannel().equals(game.getChannel())) {
			Bufferer.deleteMessage(message);
			game.setTopic(game.getMember(author));
		}
	}

	@Override
	public String getName() {
		return "refresh";
	}
}
