package com.oopsjpeg.enigma.game;

import java.time.LocalDateTime;

public interface GameAction
{
    default String execute(GameMember actor)
    {
        actor.getGame().setLastAction(LocalDateTime.now());
        actor.getGame().getAfkTimer().reset();
        return act(actor);
    }

    String act(GameMember actor);

    int getEnergy();
}