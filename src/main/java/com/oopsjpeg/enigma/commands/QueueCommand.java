package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class QueueCommand implements Command {
	@Override
	public int execute(IMessage message, String alias, String[] args) {
		IChannel channel = message.getChannel();
		IUser author = message.getAuthor();
		Player player = Enigma.getPlayer(author);

		GameMode mode = GameMode.DUEL;
		List<Player> queue = Enigma.getQueue(mode);

		if (player.getGame() == null) {
			if (!queue.contains(player)) {
				queue.add(player);
				player.setQueue(mode);
				Bufferer.sendMessage(channel, Emote.YES + author + " You are now in queue for **" + mode.getName()
						+ "** (size: **" + queue.size() + "**)");
			} else {
				queue.remove(player);
				player.clearQueue();
				Bufferer.sendMessage(channel, Emote.YES + author + " You are no longer in queue.");
			}
		}

		return SUCCESS;
	}

	@Override
	public String getName() {
		return "queue";
	}
}
