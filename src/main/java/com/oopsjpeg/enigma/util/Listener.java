package com.oopsjpeg.enigma.util;

import discord4j.core.DiscordClient;

public interface Listener {
    void register(DiscordClient client);
}
