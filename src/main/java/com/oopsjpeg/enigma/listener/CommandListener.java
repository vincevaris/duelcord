package com.oopsjpeg.enigma.listener;

import com.oopsjpeg.enigma.util.Command;
import com.oopsjpeg.enigma.util.Listener;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandListener extends ArrayList<Command> implements Listener {
    private final String prefix;

    public CommandListener(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void register(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(this::onMessage);
    }

    private void onMessage(MessageCreateEvent event) {
        DiscordClient client = event.getClient();
        Message message = event.getMessage();
        User author = message.getAuthor().orElse(null);
        String content = message.getContent().orElse(null);
        MessageChannel channel = message.getChannel().block();

        if (author != null && content != null && channel != null
                && !author.equals(client.getSelf().block())
                && content.toLowerCase().startsWith(prefix.toLowerCase())) {
            String[] split = content.replaceFirst(prefix, "").split(" ");
            String alias = split[0];
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            Command command = get(alias);

            if (command != null) command.execute(message, alias, args);
        }
    }

    public Command get(String alias) {
        return stream()
                .filter(c -> c.getName().equalsIgnoreCase(alias)
                        || Arrays.stream(c.getAliases()).anyMatch(a -> a.equalsIgnoreCase(alias)))
                .findAny().orElse(null);
    }
}
