package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.game.buff.Weaken;
import com.oopsjpeg.enigma.game.buff.Wound;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.game.unit.Berserker;
import com.oopsjpeg.enigma.game.unit.Duelist;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.ChanceBag;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.oopsjpeg.enigma.game.Stats.*;

public class GameMember {
    @Getter private final Game game;
    @Getter private final Player player;
    @Getter private Unit unit;
    @Getter @Setter private boolean alive = true;
    @Getter @Setter private boolean defensive = false;

    @Getter @Setter private List<GameObject> data = new ArrayList<>();
    @Getter @Setter private List<Item> itemHeals = new ArrayList<>();

    @Getter @Setter private ChanceBag critBag = new ChanceBag(0, 0.5f);

    @Getter @Setter private Stats stats = new Stats();

    public GameMember(Game game, Player player) {
        this.game = game;
        this.player = player;
    }

    public GameMember(GameMember other) {
        this.game = other.game;
        this.player = other.player;
        this.unit = other.unit;
        this.alive = other.alive;
        this.defensive = other.defensive;
        this.data = other.data;
        this.itemHeals = other.itemHeals;
        this.critBag = other.critBag;
        this.stats = other.stats;
    }

    public User getUser() {
        return player.getUser();
    }

    public String getUsername() {
        return getUser().getUsername();
    }

    public String getMention() {
        return getUser().getMention();
    }

    public GameObject getData(Class<?> clazz) {
        return data.stream().filter(o -> o.getClass().equals(clazz)).findAny().orElse(null);
    }

    public boolean hasData(Class<?> clazz) {
        return getData(clazz) != null;
    }

    public List<Item> getItems() {
        return data.stream()
                .filter(o -> o instanceof Item)
                .map(o -> (Item) o)
                .collect(Collectors.toList());
    }

    public List<Effect> getEffects() {
        return data.stream()
                .filter(o -> o instanceof Effect)
                .map(o -> (Effect) o)
                .collect(Collectors.toList());
    }

    public List<Buff> getBuffs() {
        return data.stream()
                .filter(o -> o instanceof Buff)
                .map(o -> (Buff) o)
                .collect(Collectors.toList());
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
        data.clear();
        data.add(unit);
        updateStats();

        stats.put(HEALTH, stats.get(MAX_HEALTH));
        stats.put(GOLD, game.getMode().handleGold(175 + (100 * game.getAlive().indexOf(this))));

        game.getCommandListener().getCommands().addAll(Arrays.asList(unit.getCommands()));

        if (unit instanceof Berserker)
            ((Berserker) unit).getRage().setCurrent(game.getAlive().indexOf(this));
    }

    public boolean hasUnit() {
        return getUnit() != null;
    }

    public void updateStats() {
        data.removeAll(getEffects());
        stats.put(MAX_HEALTH, unit.getStats().get(MAX_HEALTH));
        stats.put(Stats.DAMAGE, unit.getStats().get(Stats.DAMAGE));
        stats.put(Stats.ABILITY_POWER, unit.getStats().get(Stats.ABILITY_POWER));
        stats.put(Stats.CRIT_CHANCE, unit.getStats().get(Stats.CRIT_CHANCE));
        stats.put(Stats.CRIT_DAMAGE, unit.getStats().get(Stats.CRIT_DAMAGE));
        stats.put(Stats.LIFE_STEAL, unit.getStats().get(Stats.LIFE_STEAL));
        stats.put(Stats.RESIST, unit.getStats().get(Stats.RESIST));
        stats.put(Stats.HEALTH_PER_TURN, unit.getStats().get(Stats.HEALTH_PER_TURN));
        stats.put(Stats.GOLD_PER_TURN, unit.getStats().get(Stats.GOLD_PER_TURN));
        stats.put(Stats.ENERGY_PER_TURN, unit.getStats().get(Stats.ENERGY_PER_TURN));

        for (Item item : getItems()) {
            stats.addAll(item.getStats());
            for (Effect effect : item.getEffects())
                if (!data.contains(effect))
                    data.add(effect);
                else {
                    Effect oldEffect = (Effect) getData(effect.getClass());
                    if (effect.getPower() > oldEffect.getPower())
                        data.set(data.indexOf(oldEffect), effect);
                }
        }

        for (Effect effect : getEffects())
            stats.addAll(effect.getStats(this));

        critBag.setChance(stats.get(Stats.CRIT_CHANCE));
    }

    public void act(GameAction action) {
        if (stats.get(ENERGY) < action.getEnergy())
            Util.sendFailure(game.getChannel(), "You do not have **" + action.getEnergy() + "** energy.");
        else {
            game.getChannel().createMessage(action.execute(this)).block();
            game.getActions().add(action);
            stats.sub(ENERGY, action.getEnergy());
            if (stats.get(ENERGY) <= 0) game.nextTurn();
            else game.setTopic(this);
        }
    }

    public String buff(Buff buff) {
        if (hasData(Weaken.class)) {
            Weaken weaken = (Weaken) getData(Weaken.class);
            if (weaken.getSource().getUnit() instanceof Duelist)
                buff.setTurns(buff.getTurns() + 1);
        }

        if (hasData(buff.getClass())) {
            Buff oldBuff = (Buff) getData(buff.getClass());
            if (buff.getPower() > oldBuff.getPower())
                data.remove(oldBuff);
            else
                return null;
        }

        data.add(buff);
        return Emote.BLEED + "**" + buff.getSource().getUsername() + "** applied **" + buff.getName() + "** for **" + buff.getTurns() + "** turn(s)!";
    }

    public String shield(float amount) {
        amount *= 1 - (hasData(Wound.class) ? ((Wound) getData(Wound.class)).getPower() : 0);
        stats.add(Stats.SHIELD, amount);
        return Emote.HEAL + "**" + getUsername() + "** shielded by **" + Math.round(amount)
                + "**! [**" + stats.getInt(Stats.SHIELD) + "**]";
    }

    public String heal(float amount) {
        return heal(amount, null, true);
    }

    public String heal(float amount, String source) {
        return heal(amount, source, true);
    }

    public String heal(float amount, String source, boolean message) {
        amount *= 1 - (hasData(Wound.class) ? ((Wound) getData(Wound.class)).getPower() : 0);
        stats.add(HEALTH, amount);
        if (message) return Emote.HEAL + "**" + getUsername() + "** healed by **" + Math.round(amount) + "**! [**"
                + stats.getInt(HEALTH) + " / " + stats.getInt(MAX_HEALTH) + "**]"
                + (source == null ? "" : " (" + source + ")");
        else return null;
    }

    public String defend() {
        if (!defensive) {
            defensive = true;
            List<String> output = new ArrayList<>();
            output.add(Emote.SHIELD + "**" + getUsername() + "** is defending (**20%** resist, **" + (stats.getInt(Stats.HEALTH_PER_TURN) * 2) + "** regen)!");
            output.addAll(getData().stream().map(o -> o.onDefend(this)).collect(Collectors.toList()));
            return Util.joinNonEmpty(output);
        }
        return null;
    }

    public DamageEvent hit(DamageEvent event) {
        for (GameObject o : event.actor.data) event = o.hitOut(event);
        for (GameObject o : event.target.data) event = o.hitIn(event);

        // Life steal healing
        if (stats.get(Stats.LIFE_STEAL) > 0)
            event.heal += stats.get(Stats.LIFE_STEAL) * event.damage;

        return event;
    }

    public DamageEvent crit(DamageEvent event) {
        // Crit checks
        if (event.crit || !event.miss && critBag.get()) {
            // Pseudo RNG crit bag
            event.crit = true;

            for (GameObject o : event.actor.data) event = o.critOut(event);
            for (GameObject o : event.target.data) event = o.critIn(event);
        }

        // Critical strike bonus damage
        if (event.crit) {
            event.critMul += .5f + stats.get(Stats.CRIT_DAMAGE);
            event.damage += event.damage * event.critMul;
        }

        return event;
    }

    public DamageEvent ability(DamageEvent event) {
        for (GameObject o : event.actor.data) event = o.abilityOut(event);
        for (GameObject o : event.target.data) event = o.abilityIn(event);
        return event;
    }

    public DamageEvent basicAttack(GameMember target) {
        DamageEvent event = new DamageEvent(game, this, target);
        event.damage = stats.get(Stats.DAMAGE);
        event.actor.stats.add(GOLD, game.getMode().handleGold(Math.round(Util.nextInt(20, 30) + (game.getTurnCount() * 0.5f))));

        for (GameObject o : event.actor.data) event = o.basicAttackOut(event);
        for (GameObject o : event.target.data) event = o.basicAttackIn(event);

        event = hit(event);
        event = crit(event);

        return event;
    }

    public String damage(DamageEvent event, String emote) {
        return damage(event, emote, "");
    }

    public String damage(DamageEvent event, String emote, String source) {
        for (GameObject o : event.actor.data) event = o.damageOut(event);
        for (GameObject o : event.target.data) event = o.damageIn(event);

        event = game.getMode().handleDamage(event);

        if (event.heal > 0) event.output.add(heal(event.heal));
        if (event.shield > 0) event.output.add(shield(event.shield));

        float defend = event.target.defensive ? 0.2f : 0;
        event.damage *= 1 - event.target.getStats().get(Stats.RESIST) - defend;
        event.bonus *= 1 - event.target.getStats().get(Stats.RESIST) - defend;

        // Shield damaging
        if (event.target.stats.get(Stats.SHIELD) > 0) {
            // Remove bonus damage first
            float shdBonus = Util.limit(event.bonus, 0, event.target.stats.get(Stats.SHIELD));
            float shdDamage = 0;
            event.target.stats.sub(Stats.SHIELD, shdBonus);

            // Remove main damage after
            if (event.target.stats.get(Stats.SHIELD) > 0) {
                shdDamage = Util.limit(event.damage, 0, event.target.stats.get(Stats.SHIELD));
                event.target.stats.sub(Stats.SHIELD, shdDamage);
            }

            if (event.target.stats.get(Stats.SHIELD) > 0)
                event.output.add(0, Util.damageText(event, event.actor.getUsername(), event.target.getUsername() + "'s Shield", emote, source));
            else
                event.output.add(0, Emote.SHIELD + "**" + event.actor.getUsername() + "** destroyed **" + event.target.getUsername() + "'s Shield**!");

            event.bonus -= shdBonus;
            event.damage -= shdDamage;
        }

        if (event.target.stats.get(Stats.SHIELD) <= 0 && event.total() > 0) {
            event.target.stats.sub(HEALTH, event.total());
            event.output.add(0, Util.damageText(event, event.actor.getUsername(), event.target.getUsername(), emote, source));
            if (event.target.stats.get(HEALTH) <= 0)
                event.output.add(event.target.lose());
        }

        return Util.joinNonEmpty(event.output);
    }

    public String win() {
        game.getInstance().endGame(game);
        return Emote.TROPHY + getUser().getMention() + ", you have won the game!\n";
    }

    public String lose() {
        List<String> output = new ArrayList<>();
        output.add(Emote.SKULL + getUser().getMention() + " has been slain and removed from the game!");

        alive = false;

        if (game.getAlive().size() == 1) {
            game.setGameState(2);
            output.add(game.getAlive().get(0).win());
        } else if (game.getCurrentMember().equals(this))
            game.nextTurn();

        return Util.joinNonEmpty(output);
    }

    public float getBonusHealth() {
        return stats.get(MAX_HEALTH) - unit.getStats().get(MAX_HEALTH);
    }

    @Override
    public String toString() {
        return getPlayer().toString();
    }
}