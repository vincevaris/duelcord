package com.oopsjpeg.enigma;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.listener.CommandListener;
import com.oopsjpeg.enigma.listener.ReadyListener;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Listener;
import com.oopsjpeg.enigma.util.MongoManager;
import com.oopsjpeg.enigma.util.Settings;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Enigma {
    public static final Logger LOGGER = LoggerFactory.getLogger(Enigma.class);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    private static Enigma instance;

    private MongoManager mongo;
    private DiscordClient client;
    private Settings settings = new Settings(getSettingsFile());
    private ArrayList<Listener> listeners = new ArrayList<>();

    private CommandListener commands;
    private LinkedList<Game> games = new LinkedList<>();
    private HashMap<Long, Player> players = new HashMap<>();
    private HashMap<GameMode, LinkedList<Player>> queues = new HashMap<>();

    public static Enigma getInstance() {
        return instance;
    }

    public static File getSettingsFile() {
        return new File("enigma.properties");
    }

    public static void main(String[] args) {
        instance = new Enigma();
        instance.start();
    }

    private void start() {
        try {
            if (!settings.getFile().exists()) {
                // Create settings if it doesn't exist
                settings.save();
                LOGGER.info("Created new settings. Please configure it.");
            } else {
                // Load settings
                settings.load();
                LOGGER.info("Loaded settings.");

                // Create mongo manager
                mongo = new MongoManager(settings.get(Settings.MONGO_HOST), settings.get(Settings.MONGO_DATABASE));

                // Create discord client
                client = new DiscordClientBuilder(settings.get(Settings.TOKEN)).build();

                // Create command listener
                commands = new CommandListener(this, settings.get(Settings.MAIN_PREFIX), GeneralCommand.values());

                // Add listeners
                addListener(new ReadyListener(this));
                addListener(commands);

                // Log in client
                client.login().block();
            }
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public CommandListener getCommands() {
        return commands;
    }

    public void addListener(Listener listener) {
        listener.register(client);
        listeners.add(listener);
        LOGGER.info("Added listener of class '" + listener.getClass().getName() + "'.");
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
        LOGGER.info("Removed listener of class '" + listener.getClass().getName() + "'.");
    }

    public Player getPlayer(long id) {
        if (!players.containsKey(id))
            players.put(id, new Player(id));
        return players.get(id);
    }

    public Player getPlayer(User user) {
        return getPlayer(user.getId().asLong());
    }

    public boolean hasPlayer(User user) {
        return players.containsKey(user.getId().asLong());
    }

    public LinkedList<Player> getQueue(GameMode mode) {
        if (!queues.containsKey(mode))
            queues.put(mode, new LinkedList<>());
        return queues.get(mode);
    }

    public void refreshQueues() {
        // Loops queues for each game mode
        for (Map.Entry<GameMode, LinkedList<Player>> queue : queues.entrySet()) {
            GameMode mode = queue.getKey();
            LinkedList<Player> players = queue.getValue();
            ArrayList<Player> matched = new ArrayList<>();

            // Find players for a match
            for (Player player : players) {
                matched.add(player);

                // Create the match
                if (matched.size() >= mode.getSize()) {
                    Game game = new Game(this, mode, matched);

                    games.add(game);
                    matched.forEach(p -> {
                        p.setGame(game);
                        p.removeQueue();
                        queue.getValue().remove(p);
                    });
                    queues.get(mode).removeAll(matched);

                    Util.send(getMatchmakingChannel(), "**" + mode.getName() + "** has been found for "
                            + game.getUsers().stream().map(User::getUsername).collect(Collectors.joining(", ")),
                            "Go to " + game.getChannel().getMention() + " to play the match!");

                    break;
                }
            }
        }
    }

    public void endGame(Game game) {
        if (game.getTurnCount() > 7 && game.getMode().isRanked()) {
            Game.Member winner = game.getAlive().get(0);
            // Winner
            winner.getPlayer().win();
            winner.getPlayer().addGems(Util.limit((game.getTurnCount() / 2) + Util.nextInt(20, 40), 10, 80));
            winner.getPlayer().getUnitData(winner.getUnit().getName()).addPoints(Util.nextInt(160, 200));
            mongo.savePlayer(winner.getPlayer());
            // Losers
            game.getDead().forEach(m -> {
                m.getPlayer().lose();
                m.getPlayer().addGems(Util.limit((game.getTurnCount() / 2) + Util.nextInt(0, 10), 5, 40));
                m.getPlayer().getUnitData(m.getUnit().getName()).addPoints(Util.nextInt(80, 100));
                mongo.savePlayer(m.getPlayer());
            });
            // Send embed
            getLogChannel().createEmbed(e -> {
                LocalDateTime now = LocalDateTime.now();
                e.setColor(Color.YELLOW);
                e.setAuthor("Victory by " + winner.getUsername() + " on " + game.getMode().getName(), null, winner.getUser().getAvatarUrl());
                e.setDescription("Playing as **" + winner.getUnit().getName() + "**."
                        + "\nOpponent(s): " + game.getDead().stream().map(Game.Member::getUsername).collect(Collectors.joining(", "))
                        + "\n**" + game.getTurnCount() + "** turns and **" + game.getActions().size() + "** actions."
                        + "\n**" + winner.getPlayer().getWins() + "** wins and **" + winner.getPlayer().getLosses() + "** losses.");
                e.setFooter(now.getYear() + "/" + now.getMonthValue() + "/" + now.getDayOfMonth(), null);
            }).block();
        }

        game.getPlayers().forEach(Player::removeGame);
        listeners.remove(game.getCommandListener());
        games.remove(game);
        SCHEDULER.schedule(() -> game.getChannel().delete().block(), 2, TimeUnit.MINUTES);
    }

    public void buildUnitsChannel() {
        System.out.println("Building units channel.");
        Arrays.stream(Unit.values()).forEach(u -> getUnitsChannel().createEmbed(e -> {
            e.setTitle("**" + u.getName() + "**");
            e.setColor(u.getColor());
            String desc = "Health: **" + u.getStats().getInt(Stats.MAX_HEALTH) + "** (+**" + u.getStats().getInt(Stats.HEALTH_PER_TURN) + "**/turn)"
                    + "\nDamage: **" + u.getStats().getInt(Stats.DAMAGE) + "**"
                    + "\nEnergy: **" + u.getStats().getInt(Stats.ENERGY) + "**";
            if (u.getStats().get(Stats.CRIT_CHANCE) > 0)
                desc += "\nCritical Chance: **" + Util.percent(u.getStats().get(Stats.CRIT_CHANCE)) + "**";
            if (u.getStats().get(Stats.LIFE_STEAL) > 0)
                desc += "\nLife Steal: **" + Util.percent(u.getStats().get(Stats.LIFE_STEAL)) + "**";
            e.setDescription(desc);
            e.addField("Passives / Abilities", u.getDescription(), false);
        }).block());
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

    public void buildItemsChannel() {
        System.out.println("Building items channel.");
        getItemsChannel().createEmbed(buildItemTree(Item.Tree.CONSUMABLES)).block();
        getItemsChannel().createEmbed(buildItemTree(Item.Tree.DAMAGE)).block();
        getItemsChannel().createEmbed(buildItemTree(Item.Tree.HEALTH)).block();
        getItemsChannel().createEmbed(buildItemTree(Item.Tree.ABILITY)).block();
    }

    public Guild getGuild() {
        return client.getGuildById(Snowflake.of(settings.get(Settings.GUILD_ID))).block();
    }

    public TextChannel getMatchmakingChannel() {
        return client.getChannelById(Snowflake.of(settings.get(Settings.MATCHMAKING_ID))).cast(TextChannel.class).block();
    }

    public TextChannel getUnitsChannel() {
        return client.getChannelById(Snowflake.of(settings.get(Settings.UNITS_ID))).cast(TextChannel.class).block();
    }

    public TextChannel getItemsChannel() {
        return client.getChannelById(Snowflake.of(settings.get(Settings.ITEMS_ID))).cast(TextChannel.class).block();
    }

    public TextChannel getLogChannel() {
        return client.getChannelById(Snowflake.of(settings.get(Settings.LOG_ID))).cast(TextChannel.class).block();
    }

    public MongoManager getMongo() {
        return mongo;
    }

    public DiscordClient getClient() {
        return client;
    }

    public Settings getSettings() {
        return settings;
    }

    public List<Listener> getListeners() {
        return listeners;
    }

    public List<Game> getGames() {
        return games;
    }

    public Map<Long, Player> getPlayers() {
        return players;
    }
}
