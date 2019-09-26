package com.oopsjpeg.enigma.util;

import com.oopsjpeg.enigma.game.Stats;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {
	public static final Random RANDOM = new Random();

	public static void sendError(MessageChannel channel, String error) {
		channel.sendMessage(Emote.NO + error).complete();
	}

	public static int nextInt(int min, int max) {
		return min + RANDOM.nextInt((max - min) + 1);
	}

	public static String formatStats(Stats stats) {
		List<String> output = new ArrayList<>();
		if (stats.get(Stats.MAX_HP) > 0)
			output.add("Max Health: +**" + stats.getInt(Stats.MAX_HP) + "**");
		if (stats.get(Stats.DAMAGE) > 0)
			output.add("Damage: +**" + stats.getInt(Stats.DAMAGE) + "**");
		if (stats.get(Stats.ABILITY_POWER) > 0)
			output.add("Ability Power: +**" + Math.round(stats.get(Stats.ABILITY_POWER) * 100) + "%**");
		if (stats.get(Stats.CRIT_CHANCE) > 0)
			output.add("Critical Chance: +**" + Math.round(stats.get(Stats.CRIT_CHANCE) * 100) + "%**");
		if (stats.get(Stats.CRIT_DAMAGE) > 0)
			output.add("Critical Damage: +**" + Math.round(stats.get(Stats.CRIT_DAMAGE) * 100) + "%**");
		if (stats.get(Stats.LIFE_STEAL) > 0)
			output.add("Life Steal: **" + Math.round(stats.get(Stats.LIFE_STEAL) * 100) + "%**");
		return String.join("\n", output);
	}

	public static String formatPerTurn(Stats perTurn) {
		List<String> output = new ArrayList<>();
		if (perTurn.get(Stats.HP) > 0)
			output.add("Health/turn: +**" + perTurn.getInt(Stats.HP) + "**");
		if (perTurn.get(Stats.ENERGY) > 0)
			output.add("Energy/turn: +**" + perTurn.getInt(Stats.ENERGY) + "**");
		if (perTurn.get(Stats.GOLD) > 0)
			output.add("Gold/turn: +**" + perTurn.getInt(Stats.GOLD) + "**");
		return String.join("\n", output);
	}
}
