package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.util.Command;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;

public class BuildCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        if (args[0].equalsIgnoreCase("items"))
            Enigma.getInstance().buildItemsChannel();
        else if (args[0].equalsIgnoreCase("units"))
            Enigma.getInstance().buildUnitsChannel();
    }

    @Override
    public String getName() {
        return "build";
    }

    @Override
    public PermissionSet getPermissions() {
        return PermissionSet.of(Permission.MANAGE_GUILD);
    }
}
