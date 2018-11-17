package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class BashCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
		Game game = Enigma.getPlayer(author).getGame();
		Game.Member member = game.getMember(author);

		if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
			Bufferer.deleteMessage(message);
			if (game.getGameState() == 0)
				Util.sendError(channel, "You cannot use **Bash** until the game has started.");
			else {
				Game.Member target = game.getAlive().stream().filter(m -> !m.equals(member)).findAny().orElse(null);
				if (target == null)
					Bufferer.deleteMessage(Bufferer.sendMessage(channel,
							Emote.NO + "There is no one to use **Bash** on."), 5);
				else
					member.act(game.new BashAction(target));
			}
		}
	}

	@Override
	public String getName() {
		return "bash";
	}
}
