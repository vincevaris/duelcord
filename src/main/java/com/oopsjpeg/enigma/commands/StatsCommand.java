package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.unit.Phasebreaker;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class StatsCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel().block();
        User author = message.getAuthor().orElse(null);
        Player player = Enigma.getInstance().getPlayer(author);

        if (player == null)
            Util.sendFailure(channel, "You do not have any stats.");
        else {
            channel.createEmbed(e -> {
                e.setAuthor(author.getUsername() + "'s Stats", null, author.getAvatarUrl());
                e.setDescription("**" + player.getWins() + "**W **" + player.getLosses() + "**L (**" + Util.percent(player.getWinRate()) + "** WR)"
                        + "\nGems: **" + player.getGems() + "**");
                player.getUnitDatas().stream()
                        .min(Comparator.comparingInt(Player.UnitData::getPoints))
                        .ifPresent(best -> e.addField("Top Units", best.getUnitName() + " (" + best.getPoints() + " pts)", true));
            }).block();
        }
    }

    @Override
    public String getName() {
        return "stats";
    }
}
