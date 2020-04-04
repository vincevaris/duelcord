package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.item.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Item extends GameObject {
    private final Tree tree;
    private final String tip;
    private final int cost;
    private final Item[] build;
    private final Effect[] effects;
    private final Stats stats;

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

    public Item(String name, Tree tree, String tip, int cost, Item[] build, Effect[] effects, Stats stats) {
        super(name);
        this.tree = tree;
        this.tip = tip;
        this.cost = cost;
        this.build = build;
        this.effects = effects;
        this.stats = stats;
    }

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
                .filter(i -> tree.equals(i.tree))
                .collect(Collectors.toList());
    }

    public Build build(Collection<Item> items) {
        int reduction = 0;
        ArrayList<Item> postData = new ArrayList<>(items);

        // If item has build, calculate reductions
        for (Item item : getBuild()) {
            if (postData.contains(item)) {
                // Reduce directly
                reduction += item.cost;
                postData.remove(item);
            } else if (item.getBuild() != null) {
                // Find a reduction in the build
                Build build = item.build(postData);
                reduction += build.getReduction();
                postData = build.getPostData();
            }
        }

        return new Build(this, reduction, postData);
    }

    public String onUse(GameMember member) {
        return null;
    }

    public boolean canUse(GameMember member) {
        return false;
    }

    public boolean removeOnUse() {
        return false;
    }

    public Tree getTree() {
        return tree;
    }

    public String getTip() {
        return tip != null ? tip : "";
    }

    public boolean hasTip() {
        return tip != null;
    }

    public int getCost() {
        return cost;
    }

    public Item[] getBuild() {
        return build != null ? build : new Item[0];
    }

    public boolean hasBuild() {
        return build != null;
    }

    public Effect[] getEffects() {
        return effects != null ? effects : new Effect[0];
    }

    public Stats getStats() {
        return stats != null ? stats : new Stats();
    }

    @Override
    public String toString() {
        return getName();
    }
}
