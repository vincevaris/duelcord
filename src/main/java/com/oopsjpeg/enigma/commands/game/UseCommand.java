package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class UseCommand implements Command {
	@Override
	public void execute(Message message, String alias, String[] args) {
		User author = message.getAuthor();
		MessageChannel channel = message.getChannel();
		Game game = Enigma.getPlayer(author).getGame();
		Game.Member member = game.getMember(author);

		if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
			message.delete().complete();
			if (game.getGameState() == 0)
				Util.sendError(channel, "You cannot use items until the game has started.");
			else {
				Item item = Item.fromName(String.join(" ", args));
				if (item == null)
					Util.sendError(channel, "Invalid item. Please try again.");
				else
					member.act(game.new UseAction(item));
			}
		}
	}

	@Override
	public String getName() {
		return "use";
	}
}
