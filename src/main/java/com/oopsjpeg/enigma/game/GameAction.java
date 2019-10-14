package com.oopsjpeg.enigma.game;

import java.time.LocalDateTime;

public interface GameAction {
    default String execute(Game.Member actor) {
        actor.getGame().setLastAction(LocalDateTime.now());
        actor.getGame().setAfkNotifier(0);
        return act(actor);
    }

    String act(Game.Member actor);

    int getEnergy();
}