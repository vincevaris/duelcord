package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.Util;
import com.oopsjpeg.enigma.commands.util.Command;
import com.oopsjpeg.enigma.commands.util.CommandInput;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

import java.util.List;
import java.util.stream.Collectors;

public class ClearCommand implements Command {
	@Override
	public void execute(CommandInput input) {
		IMessage message = input.getMessage();
		IUser author = message.getAuthor();

		if (author.getStringID().equalsIgnoreCase("92296992004272128")) {
			IChannel channel = message.getChannel();

			List<IMessage> messages;
			if (channel.getModifiedPermissions(Enigma.getClient().getOurUser())
					.contains(Permissions.MANAGE_MESSAGES)) {
				messages = channel.getMessageHistory(100).stream()
						.filter(m -> m.getContent().startsWith(Enigma.PREFIX_ALL)
								|| m.getAuthor().equals(Enigma.getClient().getOurUser()))
						.collect(Collectors.toList());
				messages.add(message);
				Util.bulkDelete(channel, messages);
			} else {
				messages = channel.getMessageHistory(50).stream()
						.filter(m -> m.getAuthor().equals(Enigma.getClient().getOurUser()))
						.collect(Collectors.toList());
				messages.forEach(Util::deleteMessage);
			}

			Util.deleteMessage(5, Util.sendMessage(channel,
					author + " Cleared **" + messages.size() + "** messages."));
		}
	}

	@Override
	public String getName() {
		return "clear";
	}
}
