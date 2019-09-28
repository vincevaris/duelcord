package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

public class Wound extends Buff {
	public Wound(Game.Member source, int turns, float power) {
		super(source, turns, power);
	}

	@Override
	public String getName() {
		return "Wound";
	}

	@Override
	public String onTurnStart(Game.Member member) {
		return Emote.WOUND + "**" + getSource().getName() + "'s Wound** reduced **" + member.getName() + "**'s healing"
				+ " by **" + Util.percent(getPower()) + "** this turn.";
	}
}
