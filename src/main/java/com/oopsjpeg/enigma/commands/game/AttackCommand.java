package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.commands.util.GameCommand;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.util.Util;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class AttackCommand implements GameCommand {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		GameCommand.super.execute(message, alias, args);
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
		Game game = Enigma.getPlayer(author).getGame();
		Game.Member member = game.getMember(author);

		if (member.equals(game.getCurrentMember())) {
			if (game.getGameState() == 0)
				Util.sendError(channel, "You cannot attack until the game has started.");
			else {
				Game.Member target = game.getAlive().stream().filter(m -> !m.equals(member)).findAny().orElse(null);
				if (target == null)
					Util.sendError(channel, "There is no one to attack.");
				else
					member.act(game.new AttackAction(target));
			}
		}
	}

	@Override
	public String getName() {
		return "attack";
	}
}
