package com.oopsjpeg.enigma;

import com.oopsjpeg.enigma.commands.AvatarCommand;
import com.oopsjpeg.enigma.commands.BuildCommand;
import com.oopsjpeg.enigma.commands.QueueCommand;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.game.effects.util.Effect;
import com.oopsjpeg.enigma.game.items.util.Item;
import com.oopsjpeg.enigma.game.units.util.Unit;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.roboops.framework.RoboopsUtil;
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
	private static List<Player> players = new ArrayList<>();
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
			p.setProperty("unit_channel_id", "");
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

	public static CommandCenter getCommands() {
		return commands;
	}

	public static void buildCommands() {
		commands.clear();
		commands.add(new AvatarCommand());
		commands.add(new BuildCommand());
		commands.add(new QueueCommand());
	}

	public static List<Game> getGames() {
		return games;
	}

	public static void endGame(Game game) {
		SCHEDULER.schedule(() -> {
			game.getChannel().delete();
			game.getPlayers().forEach(Player::clearGame);
			games.remove(game);
		}, 1, TimeUnit.MINUTES);
	}

	public static List<Player> getPlayers() {
		return players;
	}

	public static Player getPlayer(IUser user) {
		if (!players.contains(user))
			players.add(new Player(user));
		return players.get(players.indexOf(user));
	}

	public static List<Player> getQueue(GameMode mode) {
		if (!queues.containsKey(mode))
			queues.put(mode, new ArrayList<>());
		return queues.get(mode);
	}

	public static void refreshQueues() {
		for (Map.Entry<GameMode, ArrayList<Player>> queue : queues.entrySet()) {
			GameMode mode = queue.getKey();
			ArrayList<Player> players = new ArrayList<>();
			for (Player player : queue.getValue()) {
				players.add(player);
				if (players.size() >= queue.getKey().getPlayers()) {
					Game game = new Game(guild, mode, players);
					games.add(game);
					queues.get(mode).removeAll(players);
					players.forEach(p -> {
						p.setGame(game);
						p.clearQueue();
						queue.getValue().remove(p);
					});
					RoboopsUtil.sendMessage(mmChannel, Emote.INFO + "**" + mode.getName() + "** has been found for "
							+ players.stream().map(Player::getName).collect(Collectors.joining(", ")) + "\n"
							+ "Go to " + game.getChannel() + " to play the game!");
				}
			}
		}
	}

	public static IGuild getGuild() {
		return guild;
	}

	public static IChannel getUnitsChannel() {
		return unitsChannel;
	}

	public static void buildUnitsChannel() {
		RoboopsUtil.bulkDelete(unitsChannel, unitsChannel.getMessageHistory());
		Arrays.stream(Unit.values()).map(u -> {
			EmbedBuilder builder = new EmbedBuilder();
			builder.withTitle(u.getName());
			builder.withColor(u.getColor());
			builder.appendDesc("Max Health: **" + u.getStats().maxHp + "** (+**" + u.getPerTurn().hp + "**/t)\n");
			builder.appendDesc("Damage: **" + u.getStats().damage + "**\n");
			if (u.getStats().critChance > 0)
				builder.appendDesc("Critical Chance: **" + Math.round(u.getStats().critChance * 100) + "%**\n");
			if (u.getStats().lifeSteal > 0)
				builder.appendDesc("Life Steal: **" + Math.round(u.getStats().lifeSteal * 100) + "%**\n");
			builder.appendField("Passives / Abilities", u.getDesc() ,true);
			return builder.build();
		}).forEach(b -> RoboopsUtil.sendMessage(unitsChannel, b));
	}

	public static IChannel getItemsChannel() {
		return itemsChannel;
	}

	public static void buildItemsChannel() {
		RoboopsUtil.bulkDelete(itemsChannel, itemsChannel.getMessageHistory());
		Arrays.stream(Item.values()).sorted(Comparator.comparingInt(Item::getCost)).map(i -> {
			EmbedBuilder builder = new EmbedBuilder();
			builder.withTitle(i.getName() + " (" + i.getCost() + "g)");
			builder.withColor(Color.CYAN);
			if (i.getStats().maxHp > 0)
				builder.appendDesc("Max Health: +**" + i.getStats().maxHp + "**\n");
			if (i.getStats().damage > 0)
				builder.appendDesc("Damage: +**" + i.getStats().damage + "**\n");
			if (i.getStats().critChance > 0)
				builder.appendDesc("Critical Chance: +**" + Math.round(i.getStats().critChance * 100) + "%**\n");
			if (i.getStats().critDamage > 0)
				builder.appendDesc("Critical Damage: +**" + Math.round(i.getStats().critDamage * 100) + "%**\n");
			if (i.getStats().lifeSteal > 0)
				builder.appendDesc("Life Steal: **" + Math.round(i.getStats().lifeSteal * 100) + "%**\n");
			if (i.getPerTurn().hp > 0)
				builder.appendDesc("Health/turn: +**" + i.getPerTurn().hp + "**\n");
			if (i.getPerTurn().energy > 0)
				builder.appendDesc("Bonus Energy: +**" + i.getPerTurn().energy + "**\n");
			if (i.getPerTurn().gold > 0)
				builder.appendDesc("Bonus Gold: +**" + i.getPerTurn().gold + "**\n");
			if (i.getEffects() != null && i.getEffects().length > 0)
				builder.appendField("Effects", String.join("\n", Arrays.stream(i.getEffects())
						.map(Effect::getName).collect(Collectors.toList())), true);
			if (i.getBuild() != null && i.getBuild().length > 0)
				builder.appendField("Build", String.join("\n", Arrays.stream(i.getBuild())
						.map(Item::getName).collect(Collectors.toList())), true);
			return builder.build();
		}).forEach(b -> RoboopsUtil.sendMessage(itemsChannel, b));
	}

	@EventSubscriber
	public void onReady(ReadyEvent e) {
		buildCommands();

		guild = client.getGuildByID(Long.parseLong(guildId));
		mmChannel = client.getChannelByID(Long.parseLong(mmChannelId));
		unitsChannel = client.getChannelByID(Long.parseLong(unitsChannelId));
		itemsChannel = client.getChannelByID(Long.parseLong(itemsChannelId));

		SCHEDULER.scheduleAtFixedRate(Enigma::refreshQueues, 5, 5, TimeUnit.SECONDS);
		SCHEDULER.scheduleAtFixedRate(() -> games.stream().filter(g -> g.getGameState() == 1)
				.forEach(Game::notifyAfk), 1, 1, TimeUnit.MINUTES);
	}
}
