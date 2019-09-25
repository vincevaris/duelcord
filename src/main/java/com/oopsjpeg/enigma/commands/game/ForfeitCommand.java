package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.util.Command;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class ForfeitCommand implements Command {
	@Override
	public void execute(Message message, String alias, String[] args) {
		User author = message.getAuthor();
		MessageChannel channel = message.getChannel();
		Game game = Enigma.getPlayer(author).getGame();

		if (channel.equals(game.getChannel())) {
			message.delete().complete();
			message.getChannel().sendMessage(Enigma.getPlayer(author)
					.getGame().getMember(author).lose()).complete();
		}
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
