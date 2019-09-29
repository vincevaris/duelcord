package com.oopsjpeg.enigma.game.item;

import com.oopsjpeg.enigma.game.DamageEvent;
import com.oopsjpeg.enigma.game.Game;
import com.oopsjpeg.enigma.game.Stats;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;

public class Shatterwand extends Item {
    public static final int BONUS_DAMAGE = 12;
    public static final float BONUS_AP = 0.75f;
    public static final int BONUS_COOLDOWN = 4;

    public static final String NAME = "Shatterwand";
    public static final String DESC = "**Use**: Deals **" + BONUS_DAMAGE + "** (+" + Util.percent(BONUS_AP) + " AP) damage and applies on-hit effects."
            + "\nRefunds **25** energy upon use."
            + "\nCan only be used once every **" + BONUS_COOLDOWN + "** turns.";
    public static final int COST = 1275;
    public static final Item[] BUILD = new Item[]{new Staff(), new Ring()};
    public static final Stats STATS = new Stats()
            .put(Stats.ABILITY_POWER, 40);

    private int bonus = 0;

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = Util.limit(bonus, -1, BONUS_COOLDOWN);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDesc() {
        return DESC;
    }

    @Override
    public int getCost() {
        return COST;
    }

    @Override
    public Item[] getBuild() {
        return BUILD;
    }

    @Override
    public Stats getStats() {
        return STATS;
    }

    @Override
    public boolean canUse(Game.Member member) {
        return bonus <= 0;
    }

    @Override
    public String onUse(Game.Member member) {
        Game.Member target = member.getGame().getAlive().stream().filter(m -> !m.equals(member)).findAny().orElse(null);
        if (target == null)
            return "There is no one to use **Shatterwand** on.";
        else {
            setBonus(BONUS_COOLDOWN);
            member.getStats().add(Stats.ENERGY, 25);
            DamageEvent event = new DamageEvent(member.getGame(), member, target);
            event.damage = BONUS_DAMAGE + (event.actor.getStats().get(Stats.ABILITY_POWER) * BONUS_AP);
            event.actor.hit(event);
            return event.target.damage(event, Emote.ATTACK, "damaged");
        }
    }

    @Override
    public String onTurnStart(Game.Member member) {
        setBonus(getBonus() - 1);
        if (bonus == 0)
            return Emote.INFO + "**" + member.getName() + "'s Shatterwand** is ready to use.";
        return "";
    }
}
