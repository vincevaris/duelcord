package com.oopsjpeg.enigma;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.rest.util.PermissionSet;

import java.util.Collection;

public interface Command {
    static Command get(Collection<Command> commands, User user, String name) {
        return commands.stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(name))
                .findAny().orElse(null);
    }

    void execute(Message message, String[] args);

    String getName();

    String getDescription();

    default PermissionSet getPermissions() {
        return PermissionSet.none();
    }
}
