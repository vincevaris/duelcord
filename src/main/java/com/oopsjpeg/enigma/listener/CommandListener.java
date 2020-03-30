package com.oopsjpeg.enigma.listener;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.util.Listener;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedList;

@RequiredArgsConstructor
public class CommandListener implements Listener {
    @Getter private final Enigma instance;
    @Getter private final String prefix;
    @Getter private final LinkedList<Command> commands;
    @Getter @Setter private TextChannel limit;

    public CommandListener(Enigma instance, String prefix, Command[] commands) {
        this.instance = instance;
        this.prefix = prefix;
        this.commands = new LinkedList<>(Arrays.asList(commands));
    }

    public CommandListener(Enigma instance, String prefix, Command[] commands, TextChannel limit) {
        this(instance, prefix, commands);
        this.limit = limit;
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
                && (limit == null || channel.equals(limit))
                && !author.equals(client.getSelf().block())
                && content.toLowerCase().startsWith(prefix.toLowerCase())) {
            String[] split = content.replaceFirst(prefix, "").split(" ");
            String alias = split[0];
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            Command command = Command.get(commands, author, alias);

            if (command != null) command.execute(message, alias, args);
        }
    }
}
