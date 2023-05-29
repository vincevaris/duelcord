package com.oopsjpeg.enigma.util;

import com.oopsjpeg.enigma.Enigma;
import discord4j.core.GatewayDiscordClient;

public interface Listener {
    void register(GatewayDiscordClient client);

    Enigma getInstance();
}
