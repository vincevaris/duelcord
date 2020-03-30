package com.oopsjpeg.enigma.listener;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.util.Listener;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ReadyListener implements Listener {
    @Getter private final Enigma instance;

    @Override
    public void register(DiscordClient client) {
        client.getEventDispatcher().on(ReadyEvent.class).subscribe(this::onReady);

        instance.getMongo().loadPlayers();

        Enigma.SCHEDULER.scheduleAtFixedRate(instance::refreshQueues, 10, 10, TimeUnit.SECONDS);
        Enigma.SCHEDULER.scheduleAtFixedRate(() -> instance.getGames().stream()
                .filter(g -> g.getGameState() == 1)
                .forEach(Game::notifyAfk), 1, 1, TimeUnit.MINUTES);

    }

    public void onReady(ReadyEvent event) {
        Enigma.LOGGER.info("Enigma is ready.");
    }
}
