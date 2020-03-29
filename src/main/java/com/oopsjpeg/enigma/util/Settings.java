package com.oopsjpeg.enigma.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Settings {
    public static final String TOKEN = "token";
    public static final String MAIN_PREFIX = "main_prefix";
    public static final String GAME_PREFIX = "game_prefix";
    public static final String MONGO_HOST = "mongo_host";
    public static final String MONGO_DATABASE = "mongo_database";
    public static final String GUILD_ID = "guild_id";
    public static final String MATCHMAKING_ID = "matchmaking_id";
    public static final String UNITS_ID = "units_id";
    public static final String ITEMS_ID = "items_id";
    public static final String LOG_ID = "log_id";
    private static final Properties DEFAULTS = new Properties();

    static {
        DEFAULTS.put(TOKEN, "");
        DEFAULTS.put(MAIN_PREFIX, ".");
        DEFAULTS.put(GAME_PREFIX, ">");
        DEFAULTS.put(MONGO_HOST, "127.0.0.1");
        DEFAULTS.put(MONGO_DATABASE, "enigma");
        DEFAULTS.put(GUILD_ID, "guild_id");
        DEFAULTS.put(MATCHMAKING_ID, "matchmaking_id");
        DEFAULTS.put(UNITS_ID, "units_id");
        DEFAULTS.put(ITEMS_ID, "items_id");
        DEFAULTS.put(LOG_ID, "log_id");
    }

    private final File file;
    private final Properties properties = new Properties();

    public Settings(File file) {
        this.file = file;
        properties.putAll(DEFAULTS);
    }

    public File getFile() {
        return file;
    }

    public void load() throws IOException {
        try (FileReader fr = new FileReader(file)) {
            properties.load(fr);
        }
    }

    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            properties.store(fw, "Enigma Settings");
        }
    }

    public String get(String key) {
        return properties.getProperty(key, "");
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public long getLong(String key) {
        return Long.parseLong(get(key));
    }

}
