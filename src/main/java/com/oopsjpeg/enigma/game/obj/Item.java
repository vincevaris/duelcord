package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.item.*;
import com.oopsjpeg.enigma.util.Cooldown;

import java.lang.reflect.InvocationTargetException;

public abstract class Item extends GameObject {
    private static final Item[] values = {
            new BloodlustBlade(),
            new BoneSpear(),
            new BronzeCutlass(),
            new CrimsonBuckler(),
            new Crystal(),
            new DawnHammer(),
            new DivinePlatemail(),
            new Gemheart(),
            new Hatchet(),
            new IronScimitar(),
            new Knife(),
            new KorasScepter(),
            new MidnightDagger(),
            new Potion(),
            new Ring(),
            new ShadowGauntlet(),
            new Shatterwand(),
            new Soulstealer(),
            new Staff(),
            new SteelMallet(),
            new WolfsTooth(),
            new BlackHalberd(),
            new ViktorsScythe()
    };

    public static Item[] values() {
        return values;
    }

    public static Item fromName(String name) {
        for (Item i : values)
            if (name.equalsIgnoreCase(i.getName()) || (name.length() >= 3
                    && i.getName().toLowerCase().startsWith(name.toLowerCase()))) {
                try {
                    return i.getClass().getConstructor().newInstance();
                } catch (IllegalAccessException | InstantiationException
                        | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        return null;
    }

    public abstract String getName();

    public String getDesc() {
        return "";
    }

    public abstract int getCost();

    public Item[] getBuild() {
        return new Item[0];
    }

    public Effect[] getEffects() {
        return new Effect[0];
    }

    public Stats getStats() {
        return new Stats();
    }

    public Stats getPerTurn() {
        return new Stats();
    }

    public Cooldown getCooldown() {
        return null;
    }

    public boolean canUse(Game.Member member) {
        return false;
    }

    public boolean removeOnUse() {
        return false;
    }

    public String onUse(Game.Member member) {
        return "";
    }

    @Override
    public String toString() {
        return getName();
    }
}
