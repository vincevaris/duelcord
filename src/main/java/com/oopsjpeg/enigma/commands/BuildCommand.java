package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.roboops.framework.commands.Command;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

import java.util.EnumSet;

public class BuildCommand implements Command {
	@Override
	public void execute(IMessage message, String alias, String[] args) {
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
	public EnumSet<Permissions> getPermissions() {
		return EnumSet.of(Permissions.MANAGE_SERVER);
	}
}
