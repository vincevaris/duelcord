package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.RoboopsUtil;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.Image;

public class AvatarCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IUser author = message.getAuthor();

		if (Enigma.getClient().getApplicationOwner().equals(author)) {
			IChannel channel = message.getChannel();
			Enigma.getClient().changeAvatar(Image.forUrl(args[0], args[1]));
			Bufferer.sendMessage(channel, Emote.INFO + author + " Attempting to apply new avatar.");
		}
	}

	@Override
	public String getName() {
		return "avatar";
	}

	@Override
	public String getDesc() {
		return "Updates the bot's avatar.";
	}
}
