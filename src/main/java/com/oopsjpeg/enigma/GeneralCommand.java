package com.oopsjpeg.enigma;

import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.game.Tree;
import com.oopsjpeg.enigma.game.object.Item;
import com.oopsjpeg.enigma.game.object.Unit;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum GeneralCommand implements Command {
    BUILD("build") {
        @Override
        public void execute(Message message, String[] args) {
            if (args[0].equalsIgnoreCase("items")) {
                TextChannel channel = Enigma.getInstance().getItemsChannel();
                //channel.createMessage(buildItemTree(Tree.CONSUMABLES)).block();
                channel.createMessage(buildItemTree(Tree.DAMAGE)).block();
                channel.createMessage(buildItemTree(Tree.HEALTH)).block();
                channel.createMessage(buildItemTree(Tree.ABILITY)).block();
            } else if (args[0].equalsIgnoreCase("units")) {
                GatewayDiscordClient client = message.getClient();
                TextChannel channel = Enigma.getInstance().getUnitsChannel();

                // Create a list of select menu options for units
                List<SelectMenu.Option> options = Arrays.stream(Unit.values())
                        .map(unit -> SelectMenu.Option.of(unit.getName(), unit.getName()))
                        .collect(Collectors.toList());

                channel.createMessage(MessageCreateSpec.builder()
                        .content("# Units\nSelect a unit using the menu below to review it.")
                        .addComponent(
                                ActionRow.of(
                                        SelectMenu.of("unit_viewer", options)
                                ))
                        .build()).block();
            }
        }

        @Override
        public PermissionSet getPermissions() {
            return PermissionSet.of(Permission.MANAGE_GUILD);
        }

        public EmbedCreateSpec buildItemTree(Tree tree) {
            EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
            embed.title("**" + tree.getName() + "**");
            embed.color(tree.getColor());
            Item.fromTree(tree).stream()
                    .filter(Item::isBuyable)
                    .sorted(Comparator.comparingInt(Item::getCost))
                    .forEach(i -> {
                        String value = (i.hasTip() ? "_" + i.getTip() + "_\n" : "")
                                + Util.formatStats(i.getStats()) + "\n"
                                + (i.hasBuild() ? "[_" + Arrays.stream(i.getBuild()).map(Item::getName).collect(Collectors.joining(", ")) + "_]" : "");
                        embed.addField(i.getName() + " (" + i.getCost() + "g)", value, true);
                    });
            return embed.build();
        }
    },
    QUEUE("q") {
        @Override
        public void execute(Message message, String[] args) {
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
                    if (player.isSpectating())
                        player.removeSpectate();
                    player.setQueue(mode);
                    Util.sendSuccess(channel, "**" + author.getUsername() + "** is in queue for **" + mode.getName() + "**.");
                }
            }
        }
    },
    STATS("stats") {
        @Override
        public void execute(Message message, String[] args) {
            MessageChannel channel = message.getChannel().block();
            User author = message.getAuthor().orElse(null);
            Player player = Enigma.getInstance().getPlayer(author);

            channel.createEmbed(e -> {
                e.setAuthor(author.getUsername() + " (" + Math.round(player.getRankedPoints()) + " RP)", null, author.getAvatarUrl());
                e.setDescription("**" + player.getWins() + "**W **" + player.getLosses() + "**L (**" + Util.percent(player.getWinRate()) + "** WR)"
                        + "\nGems: **" + player.getGems() + "**");
                if (!player.getUnitDatas().isEmpty())
                    e.addField("Top Units", player.getUnitDatas().stream()
                            .sorted(Comparator.comparingInt(Player.UnitData::getPoints).reversed())
                            .limit(3)
                            .map(data -> data.getUnitName() + " (" + data.getPoints() + " pts)")
                            .collect(Collectors.joining("\n")), true);
            }).block();
        }
    },
    LEADERBOARD("leaderboard") {
        @Override
        public void execute(Message message, String[] args) {
            MessageChannel channel = message.getChannel().block();
            channel.createEmbed(Util.leaderboard()).block();
        }
    },
    SPECTATE("spectate") {
        @Override
        public void execute(Message message, String[] args) {
            MessageChannel channel = message.getChannel().block();
            User author = message.getAuthor().orElse(null);
            Player player = Enigma.getInstance().getPlayer(author);

            if (player.isSpectating()) {
                player.removeSpectate();
                Util.sendFailure(channel, "You have stopped spectating.");
            } else if (player.isInGame()) {
                Util.sendFailure(channel, "You can't spectate while in a match.");
            } else if (args.length < 1) {
                Util.sendFailure(channel, "You must specify a user to spectate.");
            } else {
                User target = Enigma.getInstance().getClient().getUsers()
                        .filter(p -> p.getUsername().toLowerCase().startsWith(String.join(" ", args).toLowerCase()))
                        .blockFirst();
                if (target == null || target.isBot()) {
                    Util.sendFailure(channel, "That player either doesn't exist or cannot be spectated.");
                } else {
                    Player targetPlayer = Enigma.getInstance().getPlayer(target);
                    if (!targetPlayer.isInGame()) {
                        Util.sendFailure(channel, "That player isn't in a match.");
                    } else {
                        if (player.isInQueue()) player.removeQueue();

                        player.setSpectateId(target.getId().asLong());
                        Util.sendSuccess(channel, "You are now spectating **" + target.getUsername() + "**#" + target.getDiscriminator() + " in " + targetPlayer.getGame().getChannel().getMention() + ".");
                    }
                }
            }
        }
    },
    TEST("test") {
        @Override
        public void execute(Message message, String[] args) {
            message.getChannel().flatMap(channel -> channel.createMessage(MessageCreateSpec.builder()
                    .content("# Duel" +
                            "\nvincevaris **vs.** boogaloonky" +
                            "\n### Select your unit using the menu below.")
                    .addComponent(ActionRow.of(
                            SelectMenu.of("test", SelectMenu.Option.of("Gunslinger", "gunslinger"))))
                    .addComponent(ActionRow.of(
                            Button.danger("forfeit", "Forfeit"),
                            Button.primary("select", "Select")))
                    .build())).subscribe();
        }

        @Override
        public PermissionSet getPermissions() {
            return PermissionSet.of(Permission.ADMINISTRATOR);
        }
    };

    private final String name;

    GeneralCommand(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "placeholder";
    }
}
