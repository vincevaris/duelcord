package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.util.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.util.EnumSet;

public class BuildCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        if (args[0].equalsIgnoreCase("items"))
            Enigma.buildItemsChannel();
        else if (args[0].equalsIgnoreCase("units"))
            Enigma.buildUnitsChannel();
    }

    @Override
    public String getName() {
        return "build";
    }

    @Override
    public EnumSet<Permission> getPermissions() {
        return EnumSet.of(Permission.MANAGE_SERVER);
    }
}
