package com.oopsjpeg.enigma.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.util.EnumSet;

public interface Command {
    void execute(Message message, String alias, String[] args);

    String getName();

    default String[] getAliases() {
        return new String[0];
    }

    default EnumSet<Permission> getPermissions() {
        return EnumSet.noneOf(Permission.class);
    }
}
