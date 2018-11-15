package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.commands.util.GameCommand;
import com.oopsjpeg.roboops.framework.Bufferer;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class ForfeitCommand implements GameCommand {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		GameCommand.super.execute(message, alias, args);
		IUser author = message.getAuthor();
		Bufferer.sendMessage(message.getChannel(), Enigma.getPlayer(author)
				.getGame().getMember(author).lose());
	}

	@Override
	public String getName() {
		return "forfeit";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"ff"};
	}
}
