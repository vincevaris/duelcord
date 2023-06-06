package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.oopsjpeg.enigma.game.Stats.ATTACK_POWER;
import static com.oopsjpeg.enigma.game.Stats.SKILL_POWER;
import static com.oopsjpeg.enigma.game.object.Item.ALCHEMISTS_ELIXIR;
import static com.oopsjpeg.enigma.util.Util.pickRandom;

public enum Augment
{
    ENTER_THE_FORGE("Enter the Forge", "Each player has received a random **Complete Item**.") {
        @Override
        public String start(Game game)
        {
            Item[] items = Arrays.stream(Item.values())
                    .filter(item -> item.getTree() == Tree.COMPLETE)
                    .toArray(Item[]::new);
            final List<String> output = game.getMembers().stream().map(member ->
            {
                Item item = pickRandom(items);

                if (member.getItems().size() >= 5)
                    return Emote.NO + "**" + member.getUsername() + "** doesn't have space for **" + item.getName() + "**...";

                member.getItems().add(item);

                return Emote.BUY + "**" + member.getUsername() + "** received **" + item.getName() + "**.";
            }).collect(Collectors.toList());
            return Util.joinNonEmpty("\n", output);
        }
    },
    FRESHLY_BREWED("Freshly Brewed", "Each player has received **" + ALCHEMISTS_ELIXIR.getName() + "**.\n*What will happen if you drink it..?*") {
        @Override
        public String start(Game game)
        {
            final List<String> output = game.getMembers().stream().map(member ->
            {
                if (member.getItems().size() >= 5)
                    return Emote.NO + "**" + member.getUsername() + "** doesn't have space for **" + ALCHEMISTS_ELIXIR.getName() + "**...";

                member.getItems().add(ALCHEMISTS_ELIXIR);

                return Emote.BUY + "**" + member.getUsername() + "** received **" + ALCHEMISTS_ELIXIR.getName() + "**.";
            }).collect(Collectors.toList());
            return Util.joinNonEmpty("\n", output);
        }
    },
    MENDING_MAGE("Mending Mage", "Skills heal for __20%__ of damage dealt.") {
        @Override
        public String start(Game game)
        {
            game.getMembers().forEach(member -> member.addBuff(new MendingMageBuff(member, 0.2f), Emote.HEAL));
            return null;
        }

        class MendingMageBuff extends Buff {
            public MendingMageBuff(GameMember source, float power)
            {
                super("Mending Mage", false, source, 99, power);
            }

            @Override
            public DamageEvent skillOut(DamageEvent event)
            {
                event.heal += event.damage * getPower();
                event.heal += event.damage * getPower();
                return event;
            }
        }
    },
    LUCKY_BLADE("Lucky Blade", "Attacks and Skills permanently grant either __1 Attack Power__ or __1 Skill Power__.") {
        @Override
        public String start(Game game)
        {
            game.getMembers().forEach(member -> member.addBuff(new LuckyBladeBuff(member, 1), Emote.ENERGY));
            return null;
        }

        class LuckyBladeBuff extends Buff {
            private float attackPower;
            private float skillPower;

            public LuckyBladeBuff(GameMember source, float power)
            {
                super("Lucky Blade", false, source, 99, power);
            }

            @Override
            public DamageEvent damageOut(DamageEvent event)
            {
                if (event.isAttack || event.isSkill)
                {
                    int rand = Util.RANDOM.nextInt(2);

                    if (rand == 0)
                        attackPower += 1 * event.onHitScale;
                    else
                        skillPower += 1 * event.onHitScale;
                }
                return event;
            }

            @Override
            public String[] getTopic(GameMember member)
            {
                return new String[]{"Lucky Blade: " + Math.round(attackPower) + " AP, " + Math.round(skillPower) + " SP"};
            }

            @Override
            public Stats getStats()
            {
                return new Stats()
                        .put(ATTACK_POWER, attackPower)
                        .put(SKILL_POWER, skillPower);
            }
        }
    },
    BURIED_TREASURE("Buried Treasure", "Each player has received __400__ Gold. Attacks and Skills have a __10%__ chance to grant __100__-__200__ Gold.") {
        @Override
        public String start(Game game)
        {
            game.getMembers().forEach(member -> member.addBuff(new BuriedTreasureBuff(member, .1f, 100, 200), Emote.BUY));
            return null;
        }

        class BuriedTreasureBuff extends Buff
        {
            private final float chance;
            private final int minAmount;
            private final int maxAmount;

            public BuriedTreasureBuff(GameMember source, float chance, int minAmount, int maxAmount)
            {
                super("Buried Treasure", false, source, 99, chance * (minAmount + maxAmount));
                this.chance = chance;
                this.minAmount = minAmount;
                this.maxAmount = maxAmount;
            }

            @Override
            public DamageEvent damageOut(DamageEvent event)
            {
                if (event.isAttack || event.isSkill)
                {
                    float rand = Util.RANDOM.nextFloat();

                    if (rand <= chance) {
                        int randAmount = Util.nextInt(minAmount, maxAmount);
                        event.actor.giveGold(randAmount);
                        event.output.add(Emote.BUY + "**" + event.actor.getUsername() + "** found buried treasure worth __" + randAmount + "__ gold!");
                    }
                }
                return event;
            }
        }
    };

    private final String name;
    private final String description;

    Augment(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public abstract String start(Game game);

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }
}
