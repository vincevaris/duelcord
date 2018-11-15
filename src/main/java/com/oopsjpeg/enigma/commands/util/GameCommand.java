package com.oopsjpeg.enigma.commands.util;

import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;

public interface GameCommand extends Command {
	@Override
	default void execute(IMessage message, String alias, String[] args) {
		Bufferer.deleteMessage(message);
	}
}
