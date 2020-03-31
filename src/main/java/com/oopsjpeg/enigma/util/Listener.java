package com.oopsjpeg.enigma.util;

import com.oopsjpeg.enigma.Enigma;
import discord4j.core.DiscordClient;

public interface Listener {
    void register(DiscordClient client);

    Enigma getInstance();
}
