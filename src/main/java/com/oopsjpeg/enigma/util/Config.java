package com.oopsjpeg.enigma.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Config
{
    public static final String CONFIG_FILE = "DUELCORD.properties";

    private static final String BOT_TOKEN = "bot.token";

    private static final String PREFIX_PRIMARY = "prefix.primary";
    private static final String PREFIX_GAME = "prefix.game";

    private static final String ID_GUILD = "id.guild";
    private static final String ID_CHANNEL_UNITS = "id.channel.units";
    private static final String ID_CHANNEL_MATCHMAKING = "id.channel.matchmaking";
    private static final String ID_CHANNEL_MATCH_LOG = "id.channel.matchLog";
    private static final String ID_CHANNEL_LEADERBOARD = "id.channel.leaderboard";

    private static final Properties properties = new Properties();

    static
    {
        properties.put(BOT_TOKEN, "");

        properties.put(PREFIX_PRIMARY, ".");
        properties.put(PREFIX_GAME, ">");

        properties.put(ID_GUILD, "");
        properties.put(ID_CHANNEL_UNITS, "");
        properties.put(ID_CHANNEL_MATCHMAKING, "");
        properties.put(ID_CHANNEL_MATCH_LOG, "");
        properties.put(ID_CHANNEL_LEADERBOARD, "");
    }

    public static void load() throws IOException
    {
        try (FileReader fr = new FileReader(CONFIG_FILE))
        {
            properties.load(fr);
        }
    }

    public static void store() throws IOException
    {
        try (FileWriter fw = new FileWriter(CONFIG_FILE))
        {
            properties.store(fw, "DUELCORD configuration");
        }
    }

    private static <T> T get(String key, Class<T> clazz)
    {
        return (T) properties.getOrDefault(key, null);
    }

    private static String getString(String key)
    {
        return get(key, String.class);
    }

    private static int getInt(String key)
    {
        return get(key, Integer.class);
    }

    private static long getLong(String key)
    {
        return get(key, Long.class);
    }

    private static float getFloat(String key)
    {
        return get(key, Float.class);
    }

    private static double getDouble(String key)
    {
        return get(key, Double.class);
    }

    public static String getBotToken()
    {
        return getString(BOT_TOKEN);
    }

    public static String getPrimaryPrefix()
    {
        return getString(PREFIX_PRIMARY);
    }

    public static String getGamePrefix()
    {
        return getString(PREFIX_GAME);
    }

    public static String getGuildId()
    {
        return getString(ID_GUILD);
    }

    public static String getUnitsChannelId()
    {
        return getString(ID_CHANNEL_UNITS);
    }

    public static String getMatchmakingChannelId()
    {
        return getString(ID_CHANNEL_MATCHMAKING);
    }

    public static String getMatchLogChannelId()
    {
        return getString(ID_CHANNEL_MATCH_LOG);
    }

    public static String getLeaderboardChannelId()
    {
        return getString(ID_CHANNEL_LEADERBOARD);
    }
}
