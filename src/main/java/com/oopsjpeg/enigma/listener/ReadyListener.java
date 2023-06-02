package com.oopsjpeg.enigma.listener;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.GameState;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Listener;
import com.oopsjpeg.enigma.util.Util;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.MessageEditSpec;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class ReadyListener implements Listener {
    private final Enigma instance;

    public ReadyListener(Enigma instance) {
        this.instance = instance;
    }

    @Override
    public void register(GatewayDiscordClient client) {
        client.on(ReadyEvent.class).subscribe(this::onReady);

        //instance.getMongo().loadPlayers();

        Enigma.SCHEDULER.scheduleAtFixedRate(instance::refreshQueues, 12, 12, TimeUnit.SECONDS);
        Enigma.SCHEDULER.scheduleAtFixedRate(() -> instance.getPlayers().values().stream()
                .filter(Player::isInQueue)
                .filter(p -> Instant.now().isAfter(p.getQueueTime().plus(5, ChronoUnit.MINUTES)))
                .forEach(p -> {
                    p.removeQueue();
                    Util.sendFailure(p.getUser().getPrivateChannel().block(), "You've been removed from queue as there are currently no players available for that mode.");
                }), 2, 2, TimeUnit.MINUTES);
        Enigma.SCHEDULER.scheduleAtFixedRate(() -> instance.getGames().stream()
                .filter(g -> g.getGameState() == GameState.PLAYING)
                .forEach(g -> {
                    g.getAfkTimer().stack();
                    if (g.getAfkTimer().getCurrent() == 4)
                        g.getChannel().createMessage(Emote.WARN + g.getCurrentMember().getMention() + ", you have **" + (g.getAfkTimer().getMax() / 2) + "** minutes to perform an action, otherwise you will **forfeit**.").subscribe();
                    else if (g.getAfkTimer().isDone())
                        g.getChannel().createMessage(g.getCurrentMember().lose()).subscribe();
                }), 1, 1, TimeUnit.MINUTES);
        Enigma.SCHEDULER.scheduleAtFixedRate(() -> instance.getLeaderboardChannel().getMessagesBefore(Snowflake.of(Instant.now()))
                .switchIfEmpty(instance.getLeaderboardChannel().createEmbed(e -> e.setTitle("...")))
                .blockFirst()
                .edit(MessageEditSpec.builder().addEmbed(Util.leaderboard()
                        .withFooter(EmbedCreateFields.Footer.of("Updates every 10 minutes.", null)))
                        .build())
                .subscribe(), 0, 10, TimeUnit.MINUTES);
    }

    public void onReady(ReadyEvent event) {
        Enigma.LOGGER.info("Enigma is ready.");
    }

    public Enigma getInstance() {
        return this.instance;
    }
}
