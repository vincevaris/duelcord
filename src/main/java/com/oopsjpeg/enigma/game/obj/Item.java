package com.oopsjpeg.enigma.game.obj;

import com.oopsjpeg.enigma.game.Build;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.game.GameObject;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.item.*;
import com.oopsjpeg.enigma.util.Cooldown;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
            new WolfsTooth()
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
        return null;
    }

    public boolean hasTip() {
        return getTip() != null;
    }

    public String getDescription() {
        return null;
    }

    public abstract int getCost();

    public Build build(Collection<Item> items) {
        int reduction = 0;
        ArrayList<Item> postData = new ArrayList<>(items);

        // If item has build, calculate reductions
        for (Item item : getBuild()) {
            if (postData.contains(item)) {
                // Reduce directly
                reduction += item.getCost();
                postData.remove(item);
            } else if (item.hasBuild()) {
                // Find a reduction in the build
                Build build = item.build(postData);
                reduction += build.getReduction();
                postData = build.getPostData();
            }
        }

        return new Build(this, reduction, postData);
    }

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

    public boolean canUse(GameMember member) {
        return false;
    }

    public boolean removeOnUse() {
        return false;
    }

    public String onUse(GameMember member) {
        return null;
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
