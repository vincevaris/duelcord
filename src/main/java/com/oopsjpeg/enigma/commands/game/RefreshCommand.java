package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.commands.util.GameCommand;
import com.oopsjpeg.enigma.game.Game;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class RefreshCommand implements GameCommand {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		GameCommand.super.execute(message, alias, args);
		IUser author = message.getAuthor();
		Game game = Enigma.getPlayer(author).getGame();
		game.setTopic(game.getMember(author));
	}

	@Override
	public String getName() {
		return "refresh";
	}
}
