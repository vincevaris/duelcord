package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.game.buff.Weaken;
import com.oopsjpeg.enigma.game.buff.Wound;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Item;
import com.oopsjpeg.enigma.game.object.Unit;
import com.oopsjpeg.enigma.game.unit.Berserker;
import com.oopsjpeg.enigma.game.unit.Duelist;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.ChanceBag;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.oopsjpeg.enigma.game.Stats.*;

public class GameMember {
    private final Game game;
    private final Player player;
    private Unit unit;
    private boolean alive = true;
    private boolean defensive = false;

    private List<GameObject> data = new ArrayList<>();
    private List<Item> itemHeals = new ArrayList<>();

    private ChanceBag critBag = new ChanceBag(0, 0.5f);

    private Stats stats = new Stats();

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

    public Player.UnitData getUnitData() {
        return getPlayer().getUnitData(unit.getName());
    }

    public float getRankedPoints() {
        return getPlayer().getRankedPoints();
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

    public boolean hasUnit() {
        return getUnit() != null;
    }

    public void updateStats() {
        data.removeAll(getEffects());
        stats.put(MAX_HEALTH, unit.getStats().get(MAX_HEALTH));
        stats.put(DAMAGE, unit.getStats().get(DAMAGE));
        stats.put(ABILITY_POWER, unit.getStats().get(ABILITY_POWER));
        stats.put(CRIT_CHANCE, unit.getStats().get(CRIT_CHANCE));
        stats.put(CRIT_DAMAGE, unit.getStats().get(CRIT_DAMAGE));
        stats.put(LIFE_STEAL, unit.getStats().get(LIFE_STEAL));
        stats.put(RESIST, unit.getStats().get(RESIST));
        stats.put(HEALTH_PER_TURN, unit.getStats().get(HEALTH_PER_TURN));
        stats.put(GOLD_PER_TURN, unit.getStats().get(GOLD_PER_TURN));
        stats.put(ENERGY_PER_TURN, unit.getStats().get(ENERGY_PER_TURN));

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
            stats.addAll(effect.getStats());

        critBag.setChance(stats.get(CRIT_CHANCE));
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
            if (weaken.getSource().unit instanceof Duelist) {
                buff.setTotalTurns(buff.getTotalTurns() + 1);
                buff.setCurrentTurns(buff.getCurrentTurns() + 1);
            }
        }

        if (hasData(buff.getClass())) {
            Buff oldBuff = (Buff) getData(buff.getClass());
            if (buff.getPower() > oldBuff.getPower())
                data.remove(oldBuff);
            else
                return null;
        }

        data.add(buff);
        return Emote.BLEED + "**" + buff.getSource().getUsername() + "** applied **" + buff.getName() + "** "
                + (buff.hasPower() ? "(" + buff.formatPower() + ") " : "")
                + "for **" + buff.getTotalTurns() + "** turn(s)!";
    }

    public String shield(float amount) {
        amount *= 1 - (hasData(Wound.class) ? ((Wound) getData(Wound.class)).getPower() : 0);
        stats.add(SHIELD, amount);
        return Emote.HEAL + "**" + getUsername() + "** shielded by **" + Math.round(amount)
                + "**! [**" + stats.getInt(SHIELD) + "**]";
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
            List<String> output = getData().stream().map(o -> o.onDefend(this)).collect(Collectors.toList());
            output.add(Emote.SHIELD + "**" + getUsername() + "** is defending (**" + Util.percent(getResist()) + "** resist, **" + (stats.getInt(HEALTH_PER_TURN) * 2) + "** regen)!");
            return Util.joinNonEmpty("\n", output);
        }
        return null;
    }

    public DamageEvent hit(DamageEvent event) {
        for (GameObject o : event.actor.data) event = o.hitOut(event);
        for (GameObject o : event.target.data) event = o.hitIn(event);

        // Life steal healing
        if (stats.get(LIFE_STEAL) > 0)
            event.heal += stats.get(LIFE_STEAL) * event.damage;

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
            event.critMul += .5f + stats.get(CRIT_DAMAGE);
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
        event.damage = stats.get(DAMAGE);
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

        event.damage *= 1 - event.target.getResist();
        event.bonus *= 1 - event.target.getResist();

        // Shield damaging
        if (event.target.stats.get(SHIELD) > 0) {
            // Remove bonus damage first
            float shdBonus = Util.limit(event.bonus, 0, event.target.stats.get(SHIELD));
            float shdDamage = 0;
            event.target.stats.sub(SHIELD, shdBonus);

            // Remove main damage after
            if (event.target.stats.get(SHIELD) > 0) {
                shdDamage = Util.limit(event.damage, 0, event.target.stats.get(SHIELD));
                event.target.stats.sub(SHIELD, shdDamage);
            }

            if (event.target.stats.get(SHIELD) > 0)
                event.output.add(0, Util.damageText(event, event.actor.getUsername(), event.target.getUsername() + "'s Shield", emote, source));
            else
                event.output.add(Emote.SHIELD + "**" + event.actor.getUsername() + "** destroyed **" + event.target.getUsername() + "'s Shield**!");

            event.bonus -= shdBonus;
            event.damage -= shdDamage;
        }

        if (event.target.stats.get(SHIELD) <= 0 && event.total() > 0) {
            event.target.stats.sub(HEALTH, event.total());
            event.output.add(0, Util.damageText(event, event.actor.getUsername(), event.target.getUsername(), emote, source));
            if (event.target.stats.get(HEALTH) <= 0)
                event.output.add(event.target.lose());
        }

        return Util.joinNonEmpty("\n", event.output);
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
            game.setGameState(Game.FINISHED);
            output.add(game.getAlive().get(0).win());
        } else if (game.getCurrentMember().equals(this))
            game.nextTurn();

        return Util.joinNonEmpty("\n", output);
    }

    public float getResist() {
        return stats.get(RESIST) + (defensive ? 0.2f : 0);
    }

    public float getBonusDamage() {
        return stats.get(DAMAGE) - unit.getStats().get(DAMAGE);
    }

    public float getBonusHealth() {
        return stats.get(MAX_HEALTH) - unit.getStats().get(MAX_HEALTH);
    }

    @Override
    public String toString() {
        return getPlayer().toString();
    }

    public Game getGame() {
        return this.game;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Unit getUnit() {
        return this.unit;
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

    public boolean isAlive() {
        return this.alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isDefensive() {
        return this.defensive;
    }

    public void setDefensive(boolean defensive) {
        this.defensive = defensive;
    }

    public List<GameObject> getData() {
        return this.data;
    }

    public void setData(List<GameObject> data) {
        this.data = data;
    }

    public List<Item> getItemHeals() {
        return this.itemHeals;
    }

    public void setItemHeals(List<Item> itemHeals) {
        this.itemHeals = itemHeals;
    }

    public ChanceBag getCritBag() {
        return this.critBag;
    }

    public void setCritBag(ChanceBag critBag) {
        this.critBag = critBag;
    }

    public Stats getStats() {
        return this.stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }
}