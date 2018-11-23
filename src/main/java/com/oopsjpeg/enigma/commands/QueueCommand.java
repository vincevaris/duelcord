package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class QueueCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		Player player = Enigma.getPlayer(author);

		GameMode mode = GameMode.DUEL;
		List<Player> queue = Enigma.getQueue(mode);

		if (player.getGame() != null)
			Util.sendError(channel, "You are already in a match.");
		else {
			if (!queue.contains(player)) {
				player.setQueue(mode);
				Bufferer.sendMessage(channel, Emote.YES + author + " You are now in queue for **" + mode.getName()
						+ "**. (size: **" + queue.size() + "**)");
			} else {
				player.removeQueue();
				Util.sendError(channel, "You are no longer in queue.");
			}
		}
	}

	@Override
	public String getName() {
		return "queue";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"find", "mm", "q"};
	}
}
