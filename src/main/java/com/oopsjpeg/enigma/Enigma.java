package com.oopsjpeg.enigma;

import com.oopsjpeg.enigma.commands.AvatarCommand;
import com.oopsjpeg.enigma.commands.BuildCommand;
import com.oopsjpeg.enigma.commands.QueueCommand;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.item.util.Item;
import com.oopsjpeg.enigma.game.unit.util.Unit;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.commands.CommandCenter;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Enigma {
	public static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(2);
	public static final String PREFIX_ALL = "-";
	public static final String PREFIX_GAME = ">";

	private static IDiscordClient client;
	private static CommandCenter commands = new CommandCenter(PREFIX_ALL);
	private static List<Game> games = new ArrayList<>();
	private static Map<Long, Player> players = new HashMap<>();
	private static Map<GameMode, ArrayList<Player>> queues = new HashMap<>();

	private static String guildId;
	private static String mmChannelId;
	private static String unitsChannelId;
	private static String itemsChannelId;

	private static IGuild guild;
	private static IChannel mmChannel;
	private static IChannel unitsChannel;
	private static IChannel itemsChannel;

	public static void main(String[] args) throws IOException {
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

			client = new ClientBuilder().withToken(p.getProperty("token")).build();
			client.getDispatcher().registerListener(commands);
			client.getDispatcher().registerListener(new Enigma());
			client.login();
		}
	}

	public static IDiscordClient getClient() {
		return client;
	}

	private static void buildCommands() {
		commands.clear();
		commands.add(new AvatarCommand());
		commands.add(new BuildCommand());
		commands.add(new QueueCommand());
	}

	public static List<Game> getGames() {
		return games;
	}

	public static void endGame(Game game) {
		game.getPlayers().forEach(Player::removeGame);
		games.remove(game);
		SCHEDULER.schedule(() -> { //TODO add delete channel to bufferer
			game.getChannel().delete();
		}, 1, TimeUnit.MINUTES);
	}

	public static Player getPlayer(IUser user) {
		if (!players.containsKey(user.getLongID()))
			players.put(user.getLongID(), new Player(user.getLongID()));
		return players.get(user.getLongID());
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

						System.out.println(p.getUser().getName() + " - " + p.getGame());
					});
					queues.get(mode).removeAll(matched);

					Bufferer.sendMessage(mmChannel, Emote.INFO + "**" + mode.getName() + "** has been found for "
							+ game.getUsers().stream().map(IUser::getName).collect(Collectors.joining(", ")) + "\n"
							+ "Go to " + game.getChannel() + " to play the game!");

					break;
				}
			}
		}
	}

	public static IChannel getUnitsChannel() {
		return unitsChannel;
	}

	public static void buildUnitsChannel() {
		Bufferer.bulkDelete(unitsChannel, unitsChannel.getMessageHistory(100));
		Arrays.stream(Unit.values()).map(u -> {
			EmbedBuilder builder = new EmbedBuilder();
			builder.withTitle(u.getName());
			builder.withColor(u.getColor());
			builder.appendDesc("Max Health: **" + u.getStats().getInt(Stats.MAX_HP) + "** (+**" + u.getPerTurn().getInt(Stats.HP) + "**/turn)\n");
			builder.appendDesc("Damage: **" + u.getStats().getInt(Stats.DAMAGE) + "**\n");
			if (u.getStats().get(Stats.CRIT_CHANCE) > 0)
				builder.appendDesc("Critical Chance: **" + Math.round(u.getStats().get(Stats.CRIT_CHANCE) * 100) + "%**\n");
			if (u.getStats().get(Stats.LIFE_STEAL) > 0)
				builder.appendDesc("Life Steal: **" + Math.round(u.getStats().get(Stats.LIFE_STEAL) * 100) + "%**\n");
			builder.appendField("Passives / Abilities", u.getDesc(), false);
			return builder.build();
		}).forEach(b -> Bufferer.sendMessage(unitsChannel, b));
	}

	public static IChannel getItemsChannel() {
		return itemsChannel;
	}

	public static void buildItemsChannel() {
		Bufferer.bulkDelete(itemsChannel, itemsChannel.getMessageHistory(100));
		Arrays.stream(Item.values()).sorted(Comparator.comparingInt(Item::getCost)).map(i -> {
			EmbedBuilder builder = new EmbedBuilder();
			builder.withTitle(i.getName() + " (" + i.getCost() + "g)");
			builder.withColor(Color.CYAN);
			builder.appendDesc(i.getDesc() + "\n");
			builder.appendDesc(Util.formatStats(i.getStats()) + "\n");
			builder.appendDesc(Util.formatPerTurn(i.getPerTurn()));
			if (i.getEffects() != null && i.getEffects().length > 0)
				builder.appendField("Unique Effects", Arrays.stream(i.getEffects())
						.map(e -> "**" + e.getName() + "**: " + e.getDesc())
						.collect(Collectors.joining("\n")), false);
			if (i.getBuild() != null && i.getBuild().length > 0)
				builder.appendField("Build", String.join("\n", Arrays.stream(i.getBuild())
						.map(Item::getName).collect(Collectors.toList())), false);
			return builder.build();
		}).forEach(b -> Bufferer.sendMessage(itemsChannel, b));
	}

	@EventSubscriber
	public void onReady(ReadyEvent e) {
		buildCommands();

		guild = client.getGuildByID(Long.parseLong(guildId));
		mmChannel = client.getChannelByID(Long.parseLong(mmChannelId));
		unitsChannel = client.getChannelByID(Long.parseLong(unitsChannelId));
		itemsChannel = client.getChannelByID(Long.parseLong(itemsChannelId));

		SCHEDULER.scheduleAtFixedRate(Enigma::refreshQueues, 10, 10, TimeUnit.SECONDS);
		SCHEDULER.scheduleAtFixedRate(() -> games.stream().filter(g -> g.getGameState() == 1)
				.forEach(Game::notifyAfk), 1, 1, TimeUnit.MINUTES);
	}
}
