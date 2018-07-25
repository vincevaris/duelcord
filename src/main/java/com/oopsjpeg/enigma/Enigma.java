package com.oopsjpeg.enigma;

import com.oopsjpeg.enigma.commands.AvatarCommand;
import com.oopsjpeg.enigma.commands.QueueCommand;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameMode;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.roboops.framework.RoboopsUtil;
import com.oopsjpeg.roboops.framework.commands.CommandCenter;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
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
	private static Map<IUser, Player> players = new HashMap<>();
	private static Map<GameMode, ArrayList<Player>> queues = new HashMap<>();

	private static String guildId;
	private static String mmChannelId;

	private static IGuild guild;
	private static IChannel mmChannel;

	public static void main(String[] args) throws IOException {
		File f = new File("config.ini");
		Properties p = new Properties();

		if (!f.exists()) try (FileWriter fw = new FileWriter(f)) {
			p.setProperty("token", "");
			p.setProperty("guild_id", "");
			p.setProperty("mm_channel_id", "");
			p.store(fw, "Enigma config");
			System.out.println("Please setup your configuration file.");
		}
		else try (FileReader fr = new FileReader("config.ini")) {
			p.load(fr);
			guildId = p.getProperty("guild_id");
			mmChannelId = p.getProperty("mm_channel_id");

			client = new ClientBuilder().withToken(p.getProperty("token")).build();
			client.getDispatcher().registerListener(commands);
			client.getDispatcher().registerListener(new Enigma());
			client.login();
		}
	}

	public static void buildCommands() {
		commands.clear();
		commands.add(new AvatarCommand());
		commands.add(new QueueCommand());
	}

	public static IDiscordClient getClient() {
		return client;
	}

	public static CommandCenter getCommands() {
		return commands;
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
		return new ArrayList<>(players.values());
	}

	public static Player getPlayer(IUser user) {
		if (!players.containsKey(user))
			players.put(user, new Player(user));
		return players.get(user);
	}

	public static List<Player> getQueue(GameMode mode) {
		if (!queues.containsKey(mode))
			queues.put(mode, new ArrayList<>());
		return queues.get(mode);
	}

	private  void refreshQueues() {
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

	@EventSubscriber
	public void onReady(ReadyEvent e) {
		buildCommands();

		guild = client.getGuildByID(Long.parseLong(guildId));
		mmChannel = client.getChannelByID(Long.parseLong(mmChannelId));

		SCHEDULER.scheduleAtFixedRate(this::refreshQueues, 5, 5, TimeUnit.SECONDS);
		SCHEDULER.scheduleAtFixedRate(() -> games.stream().filter(g -> g.getGameState() == 1)
				.forEach(Game::notifyAfk), 1, 1, TimeUnit.MINUTES);
	}
}
