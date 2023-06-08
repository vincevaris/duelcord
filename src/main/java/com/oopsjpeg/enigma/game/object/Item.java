package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.PotionBuff;
import com.oopsjpeg.enigma.game.effect.*;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.oopsjpeg.enigma.game.Stats.*;
import static com.oopsjpeg.enigma.util.Util.percent;

public enum Item implements GameObject
{
    POTION("Potion", 50)
            {
                private final int HEAL = 120;
                private final int TURNS = 2;

                @Override
                public String getDescription()
                {
                    return "Heal for __" + HEAL + "__ over **" + TURNS + "** turns.";
                }

                @Override
                public String getTip()
                {
                    return "Heal for __" + HEAL + "__";
                }

                @Override
                public String onUse(GameMember member)
                {
                    member.addBuff(new PotionBuff(member, TURNS, HEAL), Emote.HEAL);
                    return member.heal((float) HEAL / TURNS, "Potion");
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
    ALCHEMISTS_ELIXIR("Alchemist's Elixir", 50) {
        @Override
        public String getDescription()
        {
            return "Grants a random effect.";
        }

        @Override
        public String getTip()
        {
            return "Get a random effect";
        }

        @Override
        public String onUse(GameMember member)
        {
            int rand = Util.RANDOM.nextInt(3);

            switch (rand) {
                case 0: // Attack Power and Resist elixir
                    return member.addBuff(new ElixirOfMightBuff(member, 25, 0.2f), Emote.POTION);
                case 1: // Skill Power and Dodge elixir
                    return member.addBuff(new ElixirOfWillBuff(member, 30, 0.35f), Emote.POTION);
                case 2:
                    return member.addBuff(new ElixirOfHasteBuff(member, 25, 1), Emote.POTION);
                default:
                    return "The elixir did nothing..!";
            }
        }

        @Override
        public boolean canUse(GameMember member)
        {
            return true;
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

        class ElixirOfMightBuff extends Buff {
            private final int attackPower;
            private final float resist;

            public ElixirOfMightBuff(GameMember source, int attackPower, float resist)
            {
                super("Elixir of Might", false, source, 2, attackPower + resist);
                this.attackPower = attackPower;
                this.resist = resist;
            }

            @Override
            public String onTurnStart(GameMember member)
            {
                return Emote.POTION + "**" + member.getUsername() + "** has __" + attackPower + " bonus Attack Power__ and __" + percent(resist) + " bonus Resist__.";
            }

            @Override
            public String getStatus(GameMember member)
            {
                return "Elixir of Might: " + attackPower + " bonus AP, " + percent(resist) + " bonus Resist";
            }

            @Override
            public Stats getStats()
            {
                return new Stats()
                        .put(ATTACK_POWER, attackPower)
                        .put(RESIST, resist);
            }
        }

        class ElixirOfWillBuff extends Buff {
            private final int skillPower;
            private final float dodge;

            public ElixirOfWillBuff(GameMember source, int skillPower, float dodge)
            {
                super("Elixir of Will", false, source, 2, skillPower + dodge);
                this.skillPower = skillPower;
                this.dodge = dodge;
            }

            @Override
            public String onTurnStart(GameMember member)
            {
                return Emote.POTION + "**" + member.getUsername() + "** has __" + skillPower + " bonus Skill Power__ and __" + percent(dodge) + " bonus Dodge__.";
            }

            @Override
            public String getStatus(GameMember member)
            {
                return "Elixir of Will: " + skillPower + " bonus SP, " + percent(dodge) + " bonus Dodge";
            }

            @Override
            public Stats getStats()
            {
                return new Stats()
                        .put(SKILL_POWER, skillPower)
                        .put(DODGE, dodge);
            }
        }

        class ElixirOfHasteBuff extends Buff {
            private final int maxEnergy;
            private final int cdReduction;

            public ElixirOfHasteBuff(GameMember source, int maxEnergy, int cdReduction)
            {
                super("Elixir of Haste", false, source, 2, maxEnergy + cdReduction);
                this.maxEnergy = maxEnergy;
                this.cdReduction = cdReduction;
            }

            @Override
            public String onTurnStart(GameMember member)
            {
                return Emote.POTION + "**" + member.getUsername() + "** has __" + maxEnergy + " bonus Energy__ and their Skills recharge __" + cdReduction + "__ turns faster.";
            }

            @Override
            public String getStatus(GameMember member)
            {
                return "Elixir of Haste: " + maxEnergy + " bonus Energy, " + cdReduction + " CDR";
            }

            @Override
            public Stats getStats()
            {
                return new Stats()
                        .put(MAX_ENERGY, maxEnergy)
                        .put(COOLDOWN_REDUCTION, cdReduction);
            }
        }
    },

    RING("Ring", Tree.BASIC, 200, new Stats()
            .put(SKILL_POWER, 10)),
    KNIFE("Knife", Tree.BASIC, 250, new Stats()
            .put(ATTACK_POWER, 10)),

    BONE_SPEAR("Bone Spear", Tree.ADVANCED, 500,
            new Item[]{KNIFE},
            new Effect[]{new LifewasterEffect(5, .75f)},
            new Stats()
                    .put(ATTACK_POWER, 10)) {
        @Override
        public String getTip()
        {
            return "Lower enemy healing";
        }
    },
    KORAS_AMULET("Kora's Amulet", Tree.ADVANCED, 525,
            new Item[]{RING},
            new Effect[]{new KorasWillEffect(10)},
            new Stats()
                    .put(SKILL_POWER, 20)) {
        @Override
        public String getTip()
        {
            return "More skill damage";
        }
    },
    HOLY_BAND("Holy Band", Tree.ADVANCED, 550,
            new Item[]{RING},
            new Effect[]{new DivinityEffect(10)},
            new Stats()
                    .put(SKILL_POWER, 25)) {
        @Override
        public String getTip()
        {
            return "Shield when defending";
        }
    },
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
                    .put(ATTACK_POWER, 15)) {
        @Override
        public String getTip()
        {
            return "More multi-attack damage";
        }
    },

    FAITHBREAKER("Faithbreaker", Tree.COMPLETE, 1150,
            new Item[]{KORAS_AMULET, RING},
            new Effect[]{new KorasWillEffect(12, .5f)},
            new Stats()
                    .put(SKILL_POWER, 45)) {
        @Override
        public String getTip()
        {
            return "More skill damage";
        }
    },
    CRIMSON_MIGHT("Crimson Might", Tree.COMPLETE, 1175,
            new Item[]{KORAS_AMULET, RING},
            new Effect[]{new MagicalMasteryEffect(1, 2, .03f)},
            new Stats()
                    .put(SKILL_POWER, 40)) {
        @Override
        public String getTip()
        {
            return "Lower cooldowns";
        }
    },
    DAWN_HAMMER("Dawn Hammer", Tree.COMPLETE, 1200,
            new Item[]{HOLY_BAND, KNIFE},
            new Effect[]{
                    new DivinityEffect(25, .2f),
                    new RestingFaithEffect(25)},
            new Stats()
                    .put(ATTACK_POWER, 25)
                    .put(SKILL_POWER, 30)) {
        @Override
        public String getTip()
        {
            return "Bonus energy";
        }
    },
    WOLFS_FANG("Wolf's Fang", Tree.COMPLETE, 1175,
            new Item[]{BONE_SPEAR, KNIFE},
            new Effect[]{
                    new LifewasterEffect(3, .75f),
                    new WolfbiteEffect(3, .5f)
            },
            new Stats()
                    .put(ATTACK_POWER, 35)) {
        @Override
        public String getTip()
        {
            return "Attacks Weaken enemy";
        }
    },
    SOULSTEALER("Soulstealer", Tree.COMPLETE, 1200,
            new Item[]{BLOODLUST_BLADE, KNIFE},
            new Effect[]{
                    new BloodWellEffect(.2f, 50),
            },
            new Stats()
                    .put(ATTACK_POWER, 55)
                    .put(LIFE_STEAL, .2f)) {
        @Override
        public String getTip()
        {
            return "Attacks shield";
        }
    },
    IRON_SCIMITAR("Iron Scimitar", Tree.COMPLETE, 1200,
            new Item[]{BRONZE_CUTLASS, KNIFE},
            new Effect[]{new DecimateEffect(3, .12f)},
            new Stats()
                    .put(ATTACK_POWER, 50)
                    .put(CRIT_CHANCE, .4f)) {
        @Override
        public String getTip()
        {
            return "Crits Cripple enemy";
        }
    },
    SHADOW_REAVER("Shadow Reaver", Tree.COMPLETE, 1225,
            new Item[]{MIDNIGHT_DAGGER, RING},
            new Effect[]{new EndlessStrikesEffect(.2f)},
            new Stats()
                    .put(ATTACK_POWER, 30)
                    .put(SKILL_POWER, 30)) {
        @Override
        public String getTip()
        {
            return "More multi-attack damage";
        }
    };


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

    public String getDescription()
    {
        return null;
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

    public int getCostWithBuild()
    {
        int cost = getCost();
        for (Item item : getBuild())
            cost -= item.getCost();
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
