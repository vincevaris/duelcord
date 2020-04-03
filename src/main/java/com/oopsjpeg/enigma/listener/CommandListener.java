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

import java.util.Arrays;
import java.util.LinkedList;

public class CommandListener implements Listener {
    private final Enigma instance;
    private final String prefix;
    private final LinkedList<Command> commands;
    private TextChannel limit;

    public CommandListener(Enigma instance, String prefix, Command[] commands) {
        this.instance = instance;
        this.prefix = prefix;
        this.commands = new LinkedList<>(Arrays.asList(commands));
    }

    public CommandListener(Enigma instance, String prefix, Command[] commands, TextChannel limit) {
        this(instance, prefix, commands);
        this.limit = limit;
    }

    public CommandListener(Enigma instance, String prefix, LinkedList<Command> commands) {
        this.instance = instance;
        this.prefix = prefix;
        this.commands = commands;
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

    public Enigma getInstance() {
        return this.instance;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public LinkedList<Command> getCommands() {
        return this.commands;
    }

    public TextChannel getLimit() {
        return this.limit;
    }

    public void setLimit(TextChannel limit) {
        this.limit = limit;
    }
}
