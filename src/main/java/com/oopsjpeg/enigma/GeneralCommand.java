package com.oopsjpeg.enigma;

import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;

import java.util.Comparator;

public enum GeneralCommand implements Command {
    BUILD("build") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            if (args[0].equalsIgnoreCase("items"))
                Enigma.getInstance().buildItemsChannel();
            else if (args[0].equalsIgnoreCase("units"))
                Enigma.getInstance().buildUnitsChannel();
        }

        @Override
        public PermissionSet getPermissions() {
            return PermissionSet.of(Permission.MANAGE_GUILD);
        }
    },
    QUEUE("queue", "find", "mm", "q") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            MessageChannel channel = message.getChannel().block();
            User author = message.getAuthor().orElse(null);
            Player player = Enigma.getInstance().getPlayer(author);

            if (player.getGame() != null)
                Util.sendFailure(channel, "You're already in a match.");
            else if (!channel.equals(Enigma.getInstance().getMatchmakingChannel()))
                Util.sendFailure(channel, "You must be in " + Enigma.getInstance().getMatchmakingChannel().getMention() + " to queue for games.");
            else if (player.getQueue() != null) {
                player.removeQueue();
                Util.sendFailure(channel, "You have left the queue.");
            } else {
                GameMode mode = args.length > 0 ? GameMode.fromName(args[0]) : GameMode.DUEL;
                if (mode == null)
                    Util.sendFailure(channel, "Invalid game mode.");
                else {
                    player.setQueue(mode);
                    Util.sendSuccess(channel, "**" + author.getUsername() + "** is in queue for **" + mode.getName() + "**.");
                }
            }
        }
    },
    STATS("stats") {
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
    };

    private final String[] aliases;

    GeneralCommand(String... aliases) {
        this.aliases = aliases;
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
