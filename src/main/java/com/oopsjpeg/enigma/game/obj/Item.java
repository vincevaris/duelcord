package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.item.*;
import com.oopsjpeg.enigma.util.Cooldown;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        return Arrays.stream(values())
                .filter(i -> name.equalsIgnoreCase(i.getName()) || (name.length() >= 3
                    && i.getName().toLowerCase().startsWith(name.toLowerCase())))
                .findAny().orElse(null);
    }

    public static List<Item> fromTree(Tree tree) {
        return Arrays.stream(values())
                .filter(i -> tree.equals(i.getTree()))
                .collect(Collectors.toList());
    }

    public abstract String getName();

    public abstract Tree getTree();

    public String getTip() {
        return "";
    }

    public boolean hasTip() {
        return !getTip().isEmpty();
    }

    public String getDescription() {
        return "";
    }

    public abstract int getCost();

    public Item[] getBuild() {
        return new Item[0];
    }

    public boolean hasBuild() {
        return getBuild() != null && getBuild().length > 0;
    }

    public Effect[] getEffects() {
        return new Effect[0];
    }

    public Stats getStats() {
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

    public enum Tree {
        CONSUMABLES("Consumables", new Color(168, 232, 255)),
        DAMAGE("Damage", new Color(255, 168, 168)),
        HEALTH("Health", new Color(201, 255, 168)),
        ABILITY("Ability", new Color(239, 168, 255));

        private final String name;
        private final Color color;

        Tree(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public Color getColor() {
            return color;
        }
    }
}
