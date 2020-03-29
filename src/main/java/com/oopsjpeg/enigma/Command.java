package com.oopsjpeg.enigma;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.PermissionSet;

import java.util.Arrays;
import java.util.Collection;

public interface Command {
    static Command get(Collection<Command> commands, User user, String alias) {
        return commands.stream()
                .filter(cmd -> Arrays.stream(cmd.getAliases()).anyMatch(a -> a.equalsIgnoreCase(alias)))
                .findAny().orElse(null);
    }

    void execute(Message message, String alias, String[] args);

    String[] getAliases();

    default PermissionSet getPermissions() {
        return PermissionSet.none();
    }
}
