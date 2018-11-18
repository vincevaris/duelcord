package com.oopsjpeg.enigma.game.buff;

import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.util.Emote;

public class Bleed extends Buff {
	public Bleed(Game.Member source, int turns, float power) {
		super(source, turns, power);
	}

	@Override
	public String getName() {
		return "Bleed";
	}

	@Override
	public String onTurnStart(Game.Member member) {
		return Emote.BLEED + "**" + getSource().getName() + "'s Bleed** dealt **"
				+ Math.round(getPower()) + "** damage to **" + member.getName() + "**.\n"
				+ getSource().damage(member, getPower());
	}
}
