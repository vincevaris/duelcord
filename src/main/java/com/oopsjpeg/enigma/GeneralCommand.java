package com.oopsjpeg.enigma;

import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.oopsjpeg.enigma.game.Stats.*;

public enum GeneralCommand implements Command {
    BUILD("build") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            if (args[0].equalsIgnoreCase("items")) {
                TextChannel channel = Enigma.getInstance().getItemsChannel();
                channel.bulkDelete(c -> channel.getMessagesAfter(channel.getId()));
                channel.createEmbed(buildItemTree(Item.Tree.CONSUMABLES)).block();
                channel.createEmbed(buildItemTree(Item.Tree.DAMAGE)).block();
                channel.createEmbed(buildItemTree(Item.Tree.HEALTH)).block();
                channel.createEmbed(buildItemTree(Item.Tree.ABILITY)).block();
            } else if (args[0].equalsIgnoreCase("units")) {
                TextChannel channel = Enigma.getInstance().getUnitsChannel();
                channel.bulkDelete(c -> channel.getMessagesAfter(channel.getId()));
                for (Unit unit : Unit.values()) {
                    channel.createEmbed(embed -> {
                        embed.setTitle(unit.getName());
                        embed.setColor(unit.getColor());
                        List<String> desc = new ArrayList<>();
                        Stats stats = unit.getStats();
                        desc.add("Health: **" + stats.getInt(MAX_HEALTH) + "** (+**" + stats.getInt(HEALTH_PER_TURN) + "**/turn)");
                        desc.add("Damage: **" + stats.getInt(DAMAGE) + "**");
                        desc.add("Energy: **" + stats.getInt(ENERGY) + "**");
                        if (stats.get(CRIT_CHANCE) > 0)
                            desc.add("Critical Chance: **" + Util.percent(stats.get(CRIT_CHANCE)) + "**");
                        if (stats.get(LIFE_STEAL) > 0)
                            desc.add("Life Steal: **" + Util.percent(stats.get(LIFE_STEAL)) + "**");
                        embed.setDescription(String.join("\n", desc));
                        embed.addField("Passives / Abilities", unit.getDescription(), false);
                    }).block();
                }
            }
        }

        @Override
        public PermissionSet getPermissions() {
            return PermissionSet.of(Permission.MANAGE_GUILD);
        }

        public Consumer<EmbedCreateSpec> buildItemTree(Item.Tree tree) {
            return e -> {
                e.setTitle("**" + tree.getName() + "**");
                e.setColor(tree.getColor());
                Item.fromTree(tree).stream()
                        .sorted(Comparator.comparingInt(Item::getCost))
                        .forEach(i -> {
                            String value = (i.hasTip() ? "_" + i.getTip() + "_\n" : "")
                                    + Util.formatStats(i.getStats()) + "\n"
                                    + (i.hasBuild() ? "[_" + Arrays.stream(i.getBuild()).map(Item::getName).collect(Collectors.joining(", ")) + "_]" : "");
                            e.addField(i.getName() + " (" + i.getCost() + "g)", value, true);
                        });
            };
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

    @Getter private final String[] aliases;

    GeneralCommand(String... aliases) {
        this.aliases = aliases;
    }
}
