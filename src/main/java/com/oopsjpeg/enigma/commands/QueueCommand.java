package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class QueueCommand implements Command {
	@Override
	public void execute(Message message, String alias, String[] args) {
		MessageChannel channel = message.getChannel();
		User author = message.getAuthor();
		Player player = Enigma.getPlayer(author);

		GameMode mode = GameMode.DUEL;
		List<Player> queue = Enigma.getQueue(mode);

		if (player.getGame() != null)
			Util.sendError(channel, "You are already in a match.");
		else {
			if (!queue.contains(player)) {
				player.setQueue(mode);
				channel.sendMessage(Emote.YES + author.getName() + " is now in queue for **" + mode.getName()
						+ "**. (size: **" + queue.size() + "**)").complete();
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
