package com.oopsjpeg.enigma.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum GameMode
{
    DUEL("Duel", 2, true),
    CHAOS("Chaos", 3, false)
            {
                @Override
                public int handleGold(int gold)
                {
                    return Math.round(gold * 1.5f);
                }

                @Override
                public DamageEvent handleDamage(DamageEvent event)
                {
                    Game game = event.actor.getGame();
                    if (game.getTurnCount() >= 7)
                    {
                        List<GameMember> members = new ArrayList<>(game.getMembers());
                        members.sort(Comparator.comparing(GameMember::getHealth));
                        if (members.get(0).equals(event.actor))
                        {
                            event.damage *= 1.5f;
                            event.bonus *= 1.5f;
                        }
                    }
                    return event;
                }
            };

    private final String name;
    private final int size;
    private final boolean ranked;

    GameMode(String name, int size, boolean ranked)
    {
        this.name = name;
        this.size = size;
        this.ranked = ranked;
    }

    public static GameMode fromName(String name)
    {
        return Arrays.stream(values())
                .filter(g -> name.equalsIgnoreCase(g.getName()) || (name.length() >= 3
                        && g.getName().toLowerCase().startsWith(name.toLowerCase())))
                .findAny().orElse(null);
    }

    public int handleGold(int gold)
    {
        return gold;
    }

    public DamageEvent handleDamage(DamageEvent event)
    {
        return event;
    }

    public String getName()
    {
        return this.name;
    }

    public int getSize()
    {
        return this.size;
    }

    public boolean isRanked()
    {
        return this.ranked;
    }
}
