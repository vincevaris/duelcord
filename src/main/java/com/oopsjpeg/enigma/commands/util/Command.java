package com.oopsjpeg.enigma.commands.util;

public abstract class Command {
    public abstract void execute(CommandInput input);

    public abstract String getName();
}
