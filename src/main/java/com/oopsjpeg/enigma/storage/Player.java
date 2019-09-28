package com.oopsjpeg.enigma.storage;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameMode;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class Player {
    private final long id;
    private GameMode queueMode;
    private Game game;

    public Player(long id) {
        this.id = id;
    }

    public long getID() {
        return id;
    }

    public User getUser() {
        return Enigma.getClient().getUserById(id);
    }

    public GameMode getQueueMode() {
        return queueMode;
    }

    public List<Player> getQueue() {
        return Enigma.getQueue(queueMode);
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
        return getUser().getName();
    }
}
