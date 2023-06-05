package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.PotionBuff;
import com.oopsjpeg.enigma.game.effect.*;
import com.oopsjpeg.enigma.util.Emote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.oopsjpeg.enigma.game.Stats.*;

public enum Item implements GameObject
{
    POTION("Potion", 50)
            {
                private final float HEAL = 120;
                private final int TURNS = 2;

                @Override
                public String getDescription()
                {
                    return "Heals for **" + Math.round(HEAL) + "**";
                }

                @Override
                public String onUse(GameMember member)
                {
                    member.addBuff(new PotionBuff(member, TURNS, HEAL), Emote.HEAL);
                    return member.heal(HEAL / TURNS, "Potion");
                }

                @Override
                public boolean canUse(GameMember member)
                {
                    return !member.hasBuff(PotionBuff.class);
                }

                @Override
                public boolean removeOnUse()
                {
                    return true;
                }

                @Override
                public boolean isBuyable()
                {
                    return false;
                }
            },

    RING("Ring", Tree.BASIC, 200, new Stats()
            .put(SKILL_POWER, 10)),
    KNIFE("Knife", Tree.BASIC, 250, new Stats()
            .put(ATTACK_POWER, 10)),

    BONE_SPEAR("Bone Spear", Tree.ADVANCED, 500,
            new Item[]{KNIFE},
            new Effect[]{new LifewasterEffect(5, .25f)},
            new Stats()
                    .put(ATTACK_POWER, 10)),
    KORAS_AMULET("Kora's Amulet", Tree.ADVANCED, 525,
            new Item[]{RING},
            new Effect[]{new KorasWillEffect(10)},
            new Stats()
                    .put(SKILL_POWER, 20)),
    HOLY_BAND("Holy Band", Tree.ADVANCED, 550,
            new Item[]{RING},
            new Effect[]{new DivinityEffect(10)},
            new Stats()
                    .put(SKILL_POWER, 25)),
    BLOODLUST_BLADE("Bloodlust Blade", Tree.ADVANCED, 550,
            new Item[]{KNIFE},
            new Stats()
                    .put(ATTACK_POWER, 20)
                    .put(LIFE_STEAL, .07f)),
    BRONZE_CUTLASS("Bronze Cutlass", Tree.ADVANCED, 600,
            new Item[]{KNIFE},
            new Stats()
                    .put(ATTACK_POWER, 25)
                    .put(CRIT_CHANCE, .2f)),
    MIDNIGHT_DAGGER("Midnight Dagger", Tree.ADVANCED, 600,
            new Item[]{KNIFE},
            new Effect[]{new EndlessStrikesEffect(.12f)},
            new Stats()
                    .put(ATTACK_POWER, 15)),

    FAITHBREAKER("Faithbreaker", Tree.COMPLETE, 1150,
            new Item[]{KORAS_AMULET, RING},
            new Effect[]{new KorasWillEffect(12, .5f)},
            new Stats()
                    .put(SKILL_POWER, 45)),
    CRIMSON_MIGHT("Crimson Might", Tree.COMPLETE, 1175,
            new Item[]{HOLY_BAND, RING},
            new Effect[]{new MagicalMasteryEffect(1, 2, .03f)},
            new Stats()
                    .put(SKILL_POWER, 50)),
    DAWN_HAMMER("Dawn Hammer", Tree.COMPLETE, 1200,
            new Item[]{HOLY_BAND, KNIFE},
            new Effect[]{
                    new DivinityEffect(25, .2f),
                    new RestingFaithEffect(25)},
            new Stats()
                    .put(ATTACK_POWER, 25)
                    .put(SKILL_POWER, 30)),
    WOLFS_FANG("Wolf's Fang", Tree.COMPLETE, 1200,
            new Item[]{BONE_SPEAR, KNIFE},
            new Effect[]{
                    new LifewasterEffect(3, .25f),
                    new WolfbiteEffect(5, .25f)
            },
            new Stats()
                    .put(ATTACK_POWER, 35)),
    SOULSTEALER("Soulstealer", Tree.COMPLETE, 1200,
            new Item[]{BLOODLUST_BLADE, KNIFE},
            new Effect[]{
                    new BloodWellEffect(.2f, 50),
            },
            new Stats()
                    .put(ATTACK_POWER, 55)
                    .put(LIFE_STEAL, .2f)),
    IRON_SCIMITAR("Iron Scimitar", Tree.COMPLETE, 1200,
            new Item[]{BRONZE_CUTLASS, KNIFE},
            new Effect[]{new DecimateEffect(3, .12f)},
            new Stats()
                    .put(ATTACK_POWER, 50)
                    .put(CRIT_CHANCE, .4f)),
    SHADOW_REAVER("Shadow Reaver", Tree.COMPLETE, 1225,
            new Item[]{MIDNIGHT_DAGGER, RING},
            new Effect[]{new EndlessStrikesEffect(.2f)},
            new Stats()
                    .put(ATTACK_POWER, 30)
                    .put(SKILL_POWER, 30));


    private final String name;
    private final Tree tree;
    private final int cost;
    private final Stats stats;
    private final Item[] build;
    private final Effect[] effects;

    Item(String name, int cost)
    {
        this(name, null, cost, null, null, null);
    }

    Item(String name, Tree tree, int cost, Stats stats)
    {
        this(name, tree, cost, null, null, stats);
    }

    Item(String name, Tree tree, int cost, Item[] build, Stats stats)
    {
        this(name, tree, cost, build, null, stats);
    }

    Item(String name, Tree tree, int cost, Item[] build, Effect[] effects, Stats stats)
    {
        this.name = name;
        this.tree = tree;
        this.cost = cost;
        this.stats = stats;
        this.build = build;
        this.effects = effects;
    }

    public static Item fromName(String name)
    {
        return Arrays.stream(values())
                .filter(i -> name.equalsIgnoreCase(i.getName()) || (name.length() >= 3
                        && i.getName().toLowerCase().startsWith(name.toLowerCase())))
                .findAny().orElse(null);
    }

    @Override
    public String getName()
    {
        return name;
    }

    public Build build(Collection<Item> items)
    {
        int reduction = 0;
        ArrayList<Item> postData = new ArrayList<>(items);

        // If item has build, calculate reductions
        for (Item item : getBuild())
        {
            if (postData.contains(item))
            {
                // Reduce directly
                reduction += item.cost;
                postData.remove(item);
            } else if (item.getBuild() != null)
            {
                // Find a reduction in the build
                Build build = item.build(postData);
                reduction += build.getReduction();
                postData = build.getPostData();
            }
        }

        return new Build(this, reduction, postData);
    }

    public String onUse(GameMember member)
    {
        return null;
    }

    public boolean canUse(GameMember member)
    {
        return false;
    }

    public boolean removeOnUse()
    {
        return false;
    }

    public Tree getTree()
    {
        return tree;
    }

    public String getTip()
    {
        return null;
    }

    public boolean hasTip()
    {
        return getTip() != null;
    }

    public int getCost()
    {
        return cost;
    }

    public Item[] getBuild()
    {
        return build != null ? build : new Item[0];
    }

    public boolean hasBuild()
    {
        return build != null;
    }

    public Effect[] getEffects()
    {
        return effects != null ? effects : new Effect[0];
    }

    public Stats getStats()
    {
        return stats != null ? stats : new Stats();
    }

    public boolean isBuyable()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
