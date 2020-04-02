package com.oopsjpeg.enigma.listener;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Listener;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.util.Snowflake;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ReadyListener implements Listener {
    @Getter private final Enigma instance;

    @Override
    public void register(DiscordClient client) {
        client.getEventDispatcher().on(ReadyEvent.class).subscribe(this::onReady);

        instance.getMongo().loadPlayers();

        Enigma.SCHEDULER.scheduleAtFixedRate(instance::refreshQueues, 12, 12, TimeUnit.SECONDS);
        Enigma.SCHEDULER.scheduleAtFixedRate(() -> instance.getPlayers().values().stream()
                .filter(Player::isInQueue)
                .filter(p -> Instant.now().isAfter(p.getQueueTime().plus(5, ChronoUnit.MINUTES)))
                .forEach(p -> {
                    p.removeQueue();
                    Util.sendFailure(p.getUser().getPrivateChannel().block(), "You've been removed from queue as there are currently no players available for that mode.");
                }), 2, 2, TimeUnit.MINUTES);
        Enigma.SCHEDULER.scheduleAtFixedRate(() -> instance.getGames().stream()
                .filter(g -> g.getGameState() == Game.PLAYING)
                .forEach(g -> {
                    g.getAfkTimer().stack();
                    if (g.getAfkTimer().getCurrent() == 4)
                        g.getChannel().createMessage(Emote.WARN + g.getCurrentMember().getMention() + ", you have **" + (g.getAfkTimer().getMax() / 2) + "** minutes to perform an action, otherwise you will **forfeit**.").block();
                    else if (g.getAfkTimer().isDone())
                        g.getChannel().createMessage(g.getCurrentMember().lose()).block();
                }), 1, 1, TimeUnit.MINUTES);
        Enigma.SCHEDULER.scheduleAtFixedRate(() -> instance.getLeaderboardChannel().getMessagesBefore(Snowflake.of(Instant.now()))
                .switchIfEmpty(instance.getLeaderboardChannel().createEmbed(e -> e.setTitle("...")))
                .blockFirst()
                .edit(m -> m.setEmbed(Util.leaderboard().andThen(e -> e.setFooter("Updates every 10 minutes.", null))))
                .block(), 0, 10, TimeUnit.MINUTES);
    }

    public void onReady(ReadyEvent event) {
        Enigma.LOGGER.info("Enigma is ready.");
    }
}
