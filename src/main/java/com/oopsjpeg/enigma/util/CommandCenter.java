package com.oopsjpeg.enigma.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandCenter extends ArrayList<Command> {
    private final String prefix;

    public CommandCenter(String prefix) {
        this.prefix = prefix;
    }

    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();
        String[] split = content.split(" ");

        if (split[0].toLowerCase().startsWith(prefix.toLowerCase())) {
            String alias = split[0].replaceFirst(prefix, "");
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            Command command = get(alias);
            if (command != null)
                command.execute(message, alias, args);
        }
    }

    public Command get(String alias) {
        return stream()
                .filter(c -> c.getName().equalsIgnoreCase(alias)
                        || Arrays.stream(c.getAliases()).anyMatch(a -> a.equalsIgnoreCase(alias)))
                .findAny().orElse(null);
    }
}
