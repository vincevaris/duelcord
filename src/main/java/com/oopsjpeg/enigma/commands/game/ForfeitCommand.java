package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class ForfeitCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IUser author = message.getAuthor();
		Bufferer.deleteMessage(message);
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
