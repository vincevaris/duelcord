package com.oopsjpeg.enigma;

import com.oopsjpeg.enigma.commands.BuildCommand;
import com.oopsjpeg.enigma.commands.PatchCommand;
import com.oopsjpeg.enigma.commands.QueueCommand;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.CommandCenter;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Enigma {
    public static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(2);
    public static final String PREFIX_ALL = ".";
    public static final String PREFIX_GAME = ">";

    private static JDA client;
    private static CommandCenter commands = new CommandCenter(PREFIX_ALL);
    private static List<Game> games = new ArrayList<>();
    private static Map<Long, Player> players = new HashMap<>();
    private static Map<GameMode, ArrayList<Player>> queues = new HashMap<>();

    private static String guildId;
    private static String mmChannelId;
    private static String unitsChannelId;
    private static String itemsChannelId;
    private static String logChannelId;

    private static Guild guild;
    private static TextChannel mmChannel;
    private static TextChannel unitsChannel;
    private static TextChannel itemsChannel;
    private static TextChannel logChannel;

    public static void main(String[] args) throws IOException, LoginException {
        File f = new File("config.ini");
        Properties p = new Properties();

        if (!f.exists()) try (FileWriter fw = new FileWriter(f)) {
            p.setProperty("token", "");
            p.setProperty("guild_id", "");
            p.setProperty("mm_channel_id", "");
            p.setProperty("units_channel_id", "");
            p.setProperty("items_channel_id", "");
            p.store(fw, "Enigma config");
            System.out.println("Please setup your configuration file.");
        }
        else try (FileReader fr = new FileReader("config.ini")) {
            p.load(fr);
            guildId = p.getProperty("guild_id");
            mmChannelId = p.getProperty("mm_channel_id");
            unitsChannelId = p.getProperty("units_channel_id");
            itemsChannelId = p.getProperty("items_channel_id");

            client = new JDABuilder(p.getProperty("token")).build();
            client.setEventManager(new AnnotatedEventManager());
            client.addEventListener(commands);
            client.addEventListener(new Enigma());
        }
    }

    public static JDA getClient() {
        return client;
    }

    private static void buildCommands() {
        commands.clear();
        commands.add(new BuildCommand());
        commands.add(new PatchCommand());
        commands.add(new QueueCommand());
    }

    public static List<Game> getGames() {
        return games;
    }

    public static void endGame(Game game) {
        game.getPlayers().forEach(Player::removeGame);
        client.removeEventListener(game.getCommands());
        games.remove(game);
        SCHEDULER.schedule(() -> {
            game.getChannel().delete().complete();
        }, 2, TimeUnit.MINUTES);
    }

    public static Player getPlayer(User user) {
        if (!players.containsKey(user.getIdLong()))
            players.put(user.getIdLong(), new Player(user.getIdLong()));
        return players.get(user.getIdLong());
    }

    public static List<Player> getQueue(GameMode mode) {
        if (!queues.containsKey(mode))
            queues.put(mode, new ArrayList<>());
        return queues.get(mode);
    }

    private static void refreshQueues() {
        // Loops queues for each game mode
        for (Map.Entry<GameMode, ArrayList<Player>> queue : queues.entrySet()) {
            GameMode mode = queue.getKey();
            ArrayList<Player> players = queue.getValue();
            ArrayList<Player> matched = new ArrayList<>();

            // Find players for a match
            for (Player player : players) {
                matched.add(player);

                // Create the match
                if (matched.size() >= mode.getSize()) {
                    Game game = new Game(guild, mode, matched);

                    games.add(game);
                    matched.forEach(p -> {
                        p.setGame(game);
                        p.removeQueue();
                        queue.getValue().remove(p);
                    });
                    queues.get(mode).removeAll(matched);

                    mmChannel.sendMessage(Emote.INFO + "**" + mode.getName() + "** has been found for "
                            + game.getUsers().stream().map(User::getName).collect(Collectors.joining(", ")) + "\n"
                            + "Go to " + game.getChannel().getAsMention() + " to play the game!").complete();

                    break;
                }
            }
        }
    }

    public static TextChannel getUnitsChannel() {
        return unitsChannel;
    }

    public static void buildUnitsChannel() {
        System.out.println("Building units channel.");
        Arrays.stream(Unit.values()).map(u -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(u.getName());
            builder.setColor(u.getColor());
            builder.appendDescription("Health: **" + u.getStats().getInt(Stats.MAX_HP) + "** (+**"
                    + u.getPerTurn().getInt(Stats.HP) + "**/turn)\n");
            builder.appendDescription("Damage: **" + u.getStats().getInt(Stats.DAMAGE) + "**\n");
            builder.appendDescription("Energy: **" + u.getStats().getInt(Stats.ENERGY) + "**\n");
            if (u.getStats().get(Stats.CRIT_CHANCE) > 0)
                builder.appendDescription("Critical Chance: **" + Util.percent(u.getStats().get(Stats.CRIT_CHANCE)) + "**\n");
            if (u.getStats().get(Stats.LIFE_STEAL) > 0)
                builder.appendDescription("Life Steal: **" + Util.percent(u.getStats().get(Stats.LIFE_STEAL)) + "**\n");
            builder.addField("Passives / Abilities", u.getDesc(), false);
            return builder.build();
        }).forEach(b -> unitsChannel.sendMessage(b).complete());
    }

    public static TextChannel getItemsChannel() {
        return itemsChannel;
    }

    public static void buildItemsChannel() {
        System.out.println("Building items channel.");
        Arrays.stream(Item.values()).sorted(Comparator.comparingInt(Item::getCost)).map(i -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(i.getName() + " (" + i.getCost() + "g)");
            builder.setColor(Color.CYAN);
            builder.appendDescription(i.getDesc() + "\n\n");
            builder.appendDescription(Util.formatStats(i.getStats()) + "\n");
            builder.appendDescription(Util.formatPerTurn(i.getPerTurn()));
            if (i.getEffects() != null && i.getEffects().length > 0)
                builder.addField("Unique Effects", Arrays.stream(i.getEffects())
                        .map(e -> "**" + e.getName() + "**: " + e.getDesc())
                        .collect(Collectors.joining("\n")), false);
            if (i.getBuild() != null && i.getBuild().length > 0)
                builder.addField("Build", Arrays.stream(i.getBuild())
                        .map(Item::getName).collect(Collectors.joining("\n")), false);
            return builder.build();
        }).forEach(b -> itemsChannel.sendMessage(b).complete());
    }

    public static TextChannel getLogChannel() {
        return logChannel;
    }

    @SubscribeEvent
    public void onReady(ReadyEvent event) {
        buildCommands();

        System.out.println("Ready.");

        guild = client.getGuildById(Long.parseLong(guildId));
        mmChannel = client.getTextChannelById(Long.parseLong(mmChannelId));
        unitsChannel = client.getTextChannelById(Long.parseLong(unitsChannelId));
        itemsChannel = client.getTextChannelById(Long.parseLong(itemsChannelId));

        SCHEDULER.scheduleAtFixedRate(Enigma::refreshQueues, 10, 10, TimeUnit.SECONDS);
        SCHEDULER.scheduleAtFixedRate(() -> games.stream().filter(g -> g.getGameState() == 1)
                .forEach(Game::notifyAfk), 1, 1, TimeUnit.MINUTES);
    }
}
