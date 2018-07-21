package com.oopsjpeg.enigma.commands.util;

public interface Command {
	void execute(CommandInput input);

	String getName();

	default String getDesc() {
		return "";
	}

	default String[] getAliases() {
		return new String[0];
	}
}
