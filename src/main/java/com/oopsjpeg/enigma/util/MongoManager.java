package com.oopsjpeg.enigma.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.util.JSON;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.storage.Player;
import org.bson.Document;

import java.util.function.Consumer;

public class MongoManager extends MongoClient {
    private MongoCollection<Document> players;

    public MongoManager(String host, String database) {
        super(host);
        players = getDatabase(database).getCollection("players");
    }

    public MongoCollection<Document> getPlayers() {
        return players;
    }

    public void loadPlayers() {
        players.find().forEach((Consumer<Document>) this::loadPlayer);
    }

    public void savePlayers() {
        Enigma.getInstance().getPlayers().values().forEach(this::savePlayer);
    }

    public void loadPlayer(Document d) {
        Enigma.getInstance().getPlayers().put(d.getLong("_id"), Enigma.GSON.fromJson(JSON.serialize(d), Player.class));
    }

    public void savePlayer(Player p) {
        players.replaceOne(Filters.eq(p.getId()), Document.parse(Enigma.GSON.toJson(p)), new ReplaceOptions().upsert(true));
    }
}
