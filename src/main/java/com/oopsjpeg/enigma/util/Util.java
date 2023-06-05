package com.oopsjpeg.enigma.util;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.storage.Player;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.oopsjpeg.enigma.game.Stats.*;

public class Util
{
    public static final Random RANDOM = new Random();
    public static final Color COLOR_SUCCESS = Color.of(119, 178, 85);
    public static final Color COLOR_FAILURE = Color.of(221, 46, 68);

    public static int nextInt(int min, int max)
    {
        return min + RANDOM.nextInt(max - min);
    }

    public static float nextFloat(float min, float max)
    {
        return min + RANDOM.nextFloat() * (max - min);
    }

    public static <T> T pickRandom(T[] array)
    {
        return array[RANDOM.nextInt(array.length)];
    }

    public static String formatStats(Stats stats)
    {
        List<String> output = new ArrayList<>();
        if (stats.get(MAX_HEALTH) > 0)
            output.add("Max Health: +**" + stats.getInt(MAX_HEALTH) + "**");
        if (stats.get(MAX_ENERGY) > 0)
            output.add("Max Energy: +**" + stats.getInt(MAX_ENERGY) + "**");
        if (stats.get(ATTACK_POWER) > 0)
            output.add("Attack Power: +**" + stats.getInt(ATTACK_POWER) + "**");
        if (stats.get(SKILL_POWER) > 0)
            output.add("Skill Power: +**" + stats.getInt(SKILL_POWER) + "**");
        if (stats.get(CRIT_CHANCE) > 0)
            output.add("Critical Chance: +**" + percent(stats.get(CRIT_CHANCE)) + "**");
        if (stats.get(CRIT_DAMAGE) > 0)
            output.add("Critical Damage: +**" + percent(stats.get(CRIT_DAMAGE)) + "**");
        if (stats.get(LIFE_STEAL) > 0)
            output.add("Life Steal: +**" + percent(stats.get(LIFE_STEAL)) + "**");
        if (stats.get(RESIST) > 0)
            output.add("Resist: +**" + percent(stats.get(RESIST)) + "**");
        if (stats.get(HEALTH_PER_TURN) > 0)
            output.add("Health/turn: +**" + stats.getInt(HEALTH_PER_TURN) + "**");
        if (stats.get(GOLD_PER_TURN) > 0)
            output.add("Gold/turn: +**" + stats.getInt(GOLD_PER_TURN) + "**");
        return Util.joinNonEmpty("\n", output);
    }

    public static String formatEffects(Effect[] effects)
    {
        return Arrays.stream(effects)
                .map(e -> "**" + e.getName() + "**: " + e.getDescription())
                .collect(Collectors.joining("\n"));
    }

    public static String joinWithAnd(String... array)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = array.length - 1; i >= 0; i--)
        {
            if (i == 0)
                builder.append(array[i]);
            else if (i == 1)
                builder.append(array[i]).append(" and ");
            else
                builder.append(array[i]).append(", ");
        }
        return builder.toString();
    }

    public static String joinWithAnd(List<String> list)
    {
        return joinWithAnd(list.toArray(new String[0]));
    }

    public static EmbedCreateSpec leaderboard()
    {
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        embed.author("Top 10 Players", null, Enigma.getInstance().getClient().getSelf().block().getAvatarUrl());
        embed.color(Color.YELLOW);

        AtomicInteger place = new AtomicInteger();
        embed.description(Enigma.getInstance().getPlayers().values().stream()
                .filter(p -> p.getTotalGames() > 3 && p.getRankedPoints() != 1000)
                .sorted(Comparator.comparingDouble(Player::getRankedPoints).reversed())
                .limit(10)
                .map(p -> place.incrementAndGet() + ". **" + p.getUsername() + "**#" + p.getUser().getDiscriminator() + " (" + p.getRankedPoints() + " RP)")
                .collect(Collectors.joining("\n")));

        return embed.build();
    }

    public static String percent(float x)
    {
        return Math.round(x * 100) + "%";
    }

    public static String percentRaw(float x)
    {
        return x * 100 + "%";
    }

    public static float limit(float x, float min, float max)
    {
        return Math.max(min, Math.min(max, x));
    }

    public static int limit(int x, int min, int max)
    {
        return (int) Math.ceil(limit((float) x, (float) min, (float) max));
    }

    public static String damageText(DamageEvent event, String attacker, String victim, String emote)
    {
        return damageText(event, attacker, victim, emote, "");
    }

    public static String damageText(DamageEvent event, String attacker, String victim, String emote, String source)
    {
        Stats stats = event.target.getStats();
        return emote + "**" + attacker + "** damaged **" + victim + "** by **" + Math.round(event.damage)
                + "**" + (event.bonus > 0 ? " (+" + Math.round(event.bonus) + ")" : "") + "!" + (event.crit ? " **CRIT**!" : "")
                + " [**" + (event.target.hasShield() ? event.target.getShield() : event.target.getHealth()
                + " / " + stats.getInt(MAX_HEALTH)) + "**]"
                + (!source.isEmpty() ? " (" + source + ")" : "");
    }

    public static String timeDiff(LocalDateTime date1, LocalDateTime date2)
    {
        Duration duration = Duration.between(date1, date2);
        Stack<String> stack = new Stack<>();

        if (duration.toDays() > 0) stack.push(duration.toDays() + "d");
        duration = duration.minusDays(duration.toDays());

        if (duration.toHours() > 0) stack.push(duration.toHours() + "h");
        duration = duration.minusHours(duration.toHours());

        if (duration.toMinutes() > 0) stack.push(duration.toMinutes() + "m");
        duration = duration.minusMinutes(duration.toMinutes());

        if (duration.getSeconds() > 0) stack.push(duration.getSeconds() + "s");

        return stack.stream().limit(3).collect(Collectors.joining(" "));
    }

    public static String joinNonEmpty(String delimiter, Collection<String> output)
    {
        output.removeAll(Arrays.asList("", null));
        return String.join(delimiter, output);
    }

    public static String joinNonEmpty(String delimiter, String... output)
    {
        return joinNonEmpty(delimiter, new ArrayList<>(Arrays.asList(output)));
    }

    public static String comma(int value)
    {
        return new DecimalFormat("#,###").format(value);
    }

    public static EmbedCreateSpec embed(String title, String description, Color color)
    {
        return EmbedCreateSpec.builder()
                .title(title)
                .description(description)
                .color(color)
                .build();
    }

    public static void send(MessageChannel channel, String content)
    {
        channel.createMessage(embed("", content, Color.CYAN)).subscribe();
    }

    public static void send(MessageChannel channel, String title, String content)
    {
        channel.createMessage(embed(title, content, Color.CYAN)).subscribe();
    }

    public static void sendFailure(MessageChannel channel, String content)
    {
        channel.createMessage(embed("", Emote.NO + content, COLOR_FAILURE)).subscribe();
    }

    public static void sendFailure(MessageChannel channel, String title, String content)
    {
        channel.createMessage(embed(title, Emote.NO + content, COLOR_FAILURE)).subscribe();
    }

    public static void sendSuccess(MessageChannel channel, String content)
    {
        channel.createMessage(embed("", Emote.YES + content, COLOR_SUCCESS)).subscribe();
    }

    public static void sendSuccess(MessageChannel channel, String title, String content)
    {
        channel.createMessage(embed(title, Emote.YES + content, COLOR_SUCCESS)).subscribe();
    }
}
