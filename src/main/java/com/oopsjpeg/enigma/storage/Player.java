package com.oopsjpeg.enigma.storage;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameMode;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;

import java.util.List;

public class Player {
    private final long id;
    private transient GameMode queueMode;
    private transient Game game;
    private int gems;
    private int wins;
    private int losses;

    public Player(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return Enigma.getInstance().getClient().getUserById(Snowflake.of(id)).block();
    }

    public Member getMember(Snowflake guildId) {
        return getUser().asMember(guildId).block();
    }

    public String getUsername() {
        return getUser().getUsername();
    }

    public GameMode getQueueMode() {
        return queueMode;
    }

    public List<Player> getQueue() {
        return Enigma.getInstance().getQueue(queueMode);
    }

    public void setQueue(GameMode mode) {
        if (queueMode != null) getQueue().remove(this);
        queueMode = mode;
        if (queueMode != null) getQueue().add(this);
    }

    public void removeQueue() {
        setQueue(null);
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void removeGame() {
        setGame(null);
    }

    public int getGems() {
        return gems;
    }

    public void setGems(int gems) {
        this.gems = gems;
    }

    public void addGems(int gems) {
        this.gems =+ gems;
    }

    public void removeGems(int gems) {
        this.gems -= gems;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void win() {
        wins++;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void lose() {
        losses++;
    }

    public int getTotalGames() {
        return wins + losses;
    }

    public float getWinRate() {
        return getTotalGames() > 0 ? (float) wins / getTotalGames() : 0;
    }

    @Override
    public int hashCode() {
        return getUser().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Player && ((Player) obj).id == id;
    }

    @Override
    public String toString() {
        return getUser().toString();
    }
}
