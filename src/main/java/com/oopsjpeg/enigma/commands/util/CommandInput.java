package com.oopsjpeg.enigma.commands.util;

import sx.blah.discord.handle.obj.IMessage;

public class CommandInput {
	private final IMessage message;
	private String[] args;

	public CommandInput(IMessage message, String... args) {
		this.message = message;
		this.args = args;
	}

	public IMessage getMessage() {
		return message;
	}

	public String[] getArgs() {
		return args;
	}
}
