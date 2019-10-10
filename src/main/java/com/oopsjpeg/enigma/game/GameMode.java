package com.oopsjpeg.enigma.game;

import java.util.*;

public enum GameMode {
    DUEL("Duel", 2, true),
    CHAOS("Chaos", 3, false) {
        @Override
        public int handleGold(int gold) {
            return Math.round(gold * 1.5f);
        }

        @Override
        public DamageEvent handleDamage(DamageEvent event) {
            Game game = event.actor.getGame();
            if (game.getTurnCount() >= 7) {
                List<Game.Member> members = new ArrayList<>(game.getMembers());
                members.sort(Comparator.comparing(m -> m.getStats().get(Stats.HEALTH)));
                if (members.get(0).equals(event.actor)) {
                    event.damage *= 1.5f;
                    event.bonus *= 1.5f;
                }
            }
            return event;
        }
    };

    private final String name;
    private final int size;
    private boolean ranked;

    GameMode(String name, int size, boolean ranked) {
        this.name = name;
        this.size = size;
        this.ranked = ranked;
    }

    public static GameMode fromName(String name) {
        return Arrays.stream(values())
                .filter(g -> name.equalsIgnoreCase(g.getName()) || (name.length() >= 3
                        && g.getName().toLowerCase().startsWith(name.toLowerCase())))
                .findAny().orElse(null);
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public boolean isRanked() {
        return ranked;
    }

    public int handleGold(int gold) {
        return gold;
    }

    public DamageEvent handleDamage(DamageEvent event) {
        return event;
    }
}
