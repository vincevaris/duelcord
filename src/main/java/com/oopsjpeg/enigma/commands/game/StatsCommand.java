package com.oopsjpeg.enigma.commands.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.commands.util.GameCommand;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.util.Stats;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import com.oopsjpeg.roboops.framework.Bufferer;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class StatsCommand implements GameCommand {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
		GameCommand.super.execute(message, alias, args);
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
		Game game = Enigma.getPlayer(author).getGame();
		if (game.getGameState() == 0)
			Util.sendError(channel, "You cannot check your gold until the game has started.");
		else
			Bufferer.sendMessage(channel, Emote.BUY + "**" + author.getName() + "** has **"
					+ game.getMember(author).getStats().getInt(Stats.GOLD) + "** gold.");
	}

	@Override
	public String getName() {
		return "stats";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"gold"};
	}
}
