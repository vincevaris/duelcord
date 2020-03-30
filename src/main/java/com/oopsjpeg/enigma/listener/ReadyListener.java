package com.oopsjpeg.enigma.listener;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Listener;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
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

        Enigma.SCHEDULER.scheduleAtFixedRate(instance::refreshQueues, 10, 10, TimeUnit.SECONDS);
        Enigma.SCHEDULER.scheduleAtFixedRate(() -> instance.getPlayers().values().stream()
                .filter(Player::isInQueue)
                .filter(p -> Instant.now().isAfter(p.getQueueTime().plus(5, ChronoUnit.MINUTES)))
                .forEach(p -> {
                    p.removeQueue();
                    Util.sendFailure(p.getUser().getPrivateChannel().block(),
                            "You've been removed from queue as there are currently no players available for that mode.");
                }), 2, 2, TimeUnit.MINUTES);
        Enigma.SCHEDULER.scheduleAtFixedRate(() -> instance.getGames().stream()
                .filter(g -> g.getGameState() == 1)
                .forEach(Game::notifyAfk), 1, 1, TimeUnit.MINUTES);

    }

    public void onReady(ReadyEvent event) {
        Enigma.LOGGER.info("Enigma is ready.");
    }
}
