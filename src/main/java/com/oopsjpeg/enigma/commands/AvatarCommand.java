package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Emoji;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.Util;
import com.oopsjpeg.enigma.commands.util.Command;
import com.oopsjpeg.enigma.commands.util.CommandInput;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.Image;

public class AvatarCommand implements Command {
	@Override
	public void execute(CommandInput input) {
		IMessage message = input.getMessage();
		IUser author = message.getAuthor();

		if (Enigma.getClient().getApplicationOwner().equals(author)) {
			IChannel channel = message.getChannel();
			String[] args = input.getArgs();
			Enigma.getClient().changeAvatar(Image.forUrl(args[0], args[1]));
			Util.sendMessage(channel, Emoji.INFO + author + " Attempting to apply new avatar.");
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
