package com.oopsjpeg.enigma.util;

import discord4j.core.object.entity.Message;
import discord4j.core.object.util.PermissionSet;

public interface Command {
    void execute(Message message, String alias, String[] args);

    String getName();

    default String[] getAliases() {
        return new String[0];
    }

    default PermissionSet getPermissions() {
        return PermissionSet.none();
    }
}
