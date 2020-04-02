package com.oopsjpeg.enigma.util;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.storage.Player;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.oopsjpeg.enigma.game.Stats.*;

public class Util {
    public static final Random RANDOM = new Random();
    public static final Color COLOR_SUCCESS = new Color(119, 178, 85);
    public static final Color COLOR_FAILURE = new Color(221, 46, 68);

    public static int nextInt(int min, int max) {
        return min + RANDOM.nextInt(max - min);
    }

    public static float nextFloat(float min, float max) {
        return min + RANDOM.nextFloat() * (max - min);
    }

    public static Consumer<EmbedCreateSpec> formatUnit(Unit unit) {
        return embed -> {
            embed.setTitle(unit.getName());
            embed.setColor(unit.getColor());
            List<String> desc = new ArrayList<>();
            Stats stats = unit.getStats();
            desc.add("Health: **" + stats.getInt(MAX_HEALTH) + "** (+**" + stats.getInt(HEALTH_PER_TURN) + "**/turn)");
            desc.add("Damage: **" + stats.getInt(DAMAGE) + "**");
            desc.add("Energy: **" + stats.getInt(ENERGY) + "**");
            if (stats.get(CRIT_CHANCE) > 0)
                desc.add("Critical Chance: **" + Util.percent(stats.get(CRIT_CHANCE)) + "**");
            if (stats.get(LIFE_STEAL) > 0)
                desc.add("Life Steal: **" + Util.percent(stats.get(LIFE_STEAL)) + "**");
            embed.setDescription(String.join("\n", desc));
            embed.addField("Passives / Abilities", unit.getDescription(), false);
        };
    }

    public static String formatStats(Stats stats) {
        List<String> output = new ArrayList<>();
        if (stats.get(Stats.MAX_HEALTH) > 0)
            output.add("Max Health: +**" + stats.getInt(Stats.MAX_HEALTH) + "**");
        if (stats.get(Stats.DAMAGE) > 0)
            output.add("Damage: +**" + stats.getInt(Stats.DAMAGE) + "**");
        if (stats.get(Stats.ABILITY_POWER) > 0)
            output.add("Ability Power: +**" + stats.getInt(Stats.ABILITY_POWER) + "**");
        if (stats.get(Stats.CRIT_CHANCE) > 0)
            output.add("Critical Chance: +**" + Util.percent(stats.get(Stats.CRIT_CHANCE)) + "**");
        if (stats.get(Stats.CRIT_DAMAGE) > 0)
            output.add("Critical Damage: +**" + Util.percent(stats.get(Stats.CRIT_DAMAGE)) + "**");
        if (stats.get(Stats.LIFE_STEAL) > 0)
            output.add("Life Steal: **" + Util.percent(stats.get(Stats.LIFE_STEAL)) + "**");
        if (stats.get(Stats.HEALTH_PER_TURN) > 0)
            output.add("Health/turn: +**" + stats.getInt(Stats.HEALTH_PER_TURN) + "**");
        if (stats.get(Stats.GOLD_PER_TURN) > 0)
            output.add("Gold/turn: +**" + stats.getInt(Stats.GOLD_PER_TURN) + "**");
        if (stats.get(Stats.ENERGY_PER_TURN) > 0)
            output.add("Energy/turn: +**" + stats.getInt(Stats.ENERGY_PER_TURN) + "**");
        return Util.joinNonEmpty("\n", output);
    }

    public static String formatEffects(Effect[] effects) {
        return Arrays.stream(effects)
                .map(e -> "**" + e.getName() + "**: " + e.getDescription())
                .collect(Collectors.joining("\n"));
    }

    public static Consumer<EmbedCreateSpec> leaderboard() {
        return e -> {
            e.setAuthor("Top 10 Players", null, Enigma.getInstance().getClient().getSelf().block().getAvatarUrl());
            e.setColor(Color.YELLOW);

            AtomicInteger place = new AtomicInteger();
            e.setDescription(Enigma.getInstance().getPlayers().values().stream()
                    .filter(p -> p.getTotalGames() > 3 && p.getRankedPoints() != 1000)
                    .sorted(Comparator.comparingDouble(Player::getRankedPoints).reversed())
                    .limit(10)
                    .map(p -> place.incrementAndGet() + ". **" + p.getUsername() + "**#" + p.getUser().getDiscriminator() + " (" + p.getRankedPoints() + " RP)")
                    .collect(Collectors.joining("\n")));
        };
    }

    public static String percent(float x) {
        return Math.round(x * 100) + "%";
    }

    public static float limit(float x, float min, float max) {
        return Math.max(min, Math.min(max, x));
    }

    public static int limit(int x, int min, int max) {
        return (int) Math.ceil(limit((float) x, (float) min, (float) max));
    }

    public static String damageText(DamageEvent event, String attacker, String victim, String emote) {
        return damageText(event, attacker, victim, emote, "");
    }

    public static String damageText(DamageEvent event, String attacker, String victim, String emote, String source) {
        return emote + "**" + attacker + "** damaged **" + victim + "** by **" + Math.round(event.damage)
                + "**" + (event.bonus > 0 ? " (+" + Math.round(event.bonus) + ")" : "") + "!" + (event.crit ? " **CRIT**!" : "")
                + (event.miss ? " **MISS**!" : "") + " [**" + event.target.getStats().getInt(Stats.HEALTH)
                + " / " + event.target.getStats().getInt(Stats.MAX_HEALTH) + "**]"
                + (!source.isEmpty() ? " (" + source + ")" : "");
    }

    public static String timeDiff(LocalDateTime date1, LocalDateTime date2) {
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

    public static String joinNonEmpty(String delimiter, Collection<String> output) {
        output.removeAll(Arrays.asList("", null));
        return String.join(delimiter, output);
    }

    public static String joinNonEmpty(String delimiter, String... output) {
        return joinNonEmpty(delimiter, new ArrayList<>(Arrays.asList(output)));
    }

    public static String comma(int value) {
        return new DecimalFormat("#,###").format(value);
    }

    public static Consumer<? super EmbedCreateSpec> embed(String title, String description, Color color) {
        return e -> e
                .setTitle(title)
                .setDescription(description)
                .setColor(color);
    }

    public static void send(MessageChannel channel, String content) {
        channel.createMessage(m -> m.setEmbed(embed("", content, Color.CYAN))).block();
    }

    public static void send(MessageChannel channel, String title, String content) {
        channel.createMessage(m -> m.setEmbed(embed(title, content, Color.CYAN))).block();
    }

    public static void sendFailure(MessageChannel channel, String content) {
        channel.createMessage(m -> m.setEmbed(embed("", Emote.NO + content, COLOR_FAILURE))).block();
    }

    public static void sendFailure(MessageChannel channel, String title, String content) {
        channel.createMessage(m -> m.setEmbed(embed(title, Emote.NO + content, COLOR_FAILURE))).block();
    }

    public static void sendSuccess(MessageChannel channel, String content) {
        channel.createMessage(m -> m.setEmbed(embed("", Emote.YES + content, COLOR_SUCCESS))).block();
    }

    public static void sendSuccess(MessageChannel channel, String title, String content) {
        channel.createMessage(m -> m.setEmbed(embed(title, Emote.YES + content, COLOR_SUCCESS))).block();
    }
}
