package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Emote;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.roboops.framework.RoboopsUtil;
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

		RoboopsUtil.deleteMessage(message);
		if (player.getGame() == null) {
			if (!queue.contains(player)) {
				queue.add(player);
				player.setQueue(mode);
				RoboopsUtil.sendMessage(channel, Emote.YES + author + " You are now in queue for **" + mode.getName()
						+ "** (size: **" + queue.size() + "**)");
			} else {
				queue.remove(player);
				player.clearQueue();
				RoboopsUtil.sendMessage(channel, Emote.YES + author + " You are no longer in queue.");
			}
		}
	}

	@Override
	public String getName() {
		return "queue";
	}
}
