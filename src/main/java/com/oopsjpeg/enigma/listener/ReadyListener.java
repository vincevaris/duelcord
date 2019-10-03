package com.oopsjpeg.enigma.listener;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.util.Listener;
import com.oopsjpeg.enigma.game.Game;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;

import java.util.concurrent.TimeUnit;

public class ReadyListener implements Listener {
    @Override
    public void register(DiscordClient client) {
        client.getEventDispatcher().on(ReadyEvent.class).subscribe(this::onReady);

        Enigma.SCHEDULER.scheduleAtFixedRate(() -> Enigma.getInstance().refreshQueues(), 10, 10, TimeUnit.SECONDS);
        Enigma.SCHEDULER.scheduleAtFixedRate(() -> Enigma.getInstance().getGames().stream().filter(g -> g.getGameState() == 1)
                .forEach(Game::notifyAfk), 1, 1, TimeUnit.MINUTES);
    }

    public void onReady(ReadyEvent event) {
        Enigma.LOGGER.info("Enigma is ready.");
    }
}
