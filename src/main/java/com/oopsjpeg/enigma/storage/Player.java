package com.oopsjpeg.enigma.storage;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameMode;
import sx.blah.discord.handle.obj.IUser;

public class Player {
    private final IUser user;
    private GameMode queue;
    private Game game;

    public Player(IUser user) {
        this.user = user;
    }

    public IUser getUser() {
        return user;
    }

    public String getName() {
        return user.getName();
    }

    public GameMode getQueue() {
        return queue;
    }

    public void setQueue(GameMode queue) {
        this.queue = queue;
    }

    public void clearQueue() {
        queue = null;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void clearGame() {
        game = null;
    }

    @Override
    public String toString() {
        return user.toString();
    }
}
