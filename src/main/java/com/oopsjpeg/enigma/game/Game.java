package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.game.buff.Weaken;
import com.oopsjpeg.enigma.game.buff.Wound;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.game.unit.Berserker;
import com.oopsjpeg.enigma.game.unit.Duelist;
import com.oopsjpeg.enigma.listener.CommandListener;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.ChanceBag;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Settings;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.oopsjpeg.enigma.game.Stats.*;

public class Game {
    public static final int PICKING = 0;
    public static final int PLAYING = 1;
    private final Enigma instance;
    private final TextChannel channel;
    private final GameMode mode;
    private final List<Member> members;
    private final CommandListener commands;

    private List<GameAction> actions = new ArrayList<>();
    private LocalDateTime lastAction = LocalDateTime.now();
    private int afkNotifier = 0;

    private int gameState = PICKING;
    private int turnCount = 0;
    private int turnIndex = -1;

    public Game(Enigma instance, GameMode mode, List<Player> players) {
        this.instance = instance;
        this.mode = mode;

        channel = instance.getGuild().createTextChannel(c -> c.setName("game")).block();

        Snowflake roleId = getGuild().getEveryoneRole().block().getId();
        channel.addRoleOverwrite(roleId, PermissionOverwrite.forRole(roleId,
                PermissionSet.none(), PermissionSet.of(Permission.VIEW_CHANNEL))).block();
        players.forEach(p -> channel.addMemberOverwrite(Snowflake.of(p.getId()), PermissionOverwrite.forMember(Snowflake.of(p.getId()),
                PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none())).block());

        commands = new CommandListener(instance,
                instance.getSettings().get(Settings.GAME_PREFIX),
                GameCommand.values(), channel);
        instance.addListener(commands);

        members = players.stream().map(Member::new).collect(Collectors.toList());
        Collections.shuffle(members);

        nextTurn();
    }

    public void nextTurn() {
        List<String> output = new ArrayList<>();

        // Handle turn ending
        if (gameState == PLAYING) {
            // On turn end
            output.addAll(getCurrentMember().data.stream().map(e -> e.onTurnEnd(getCurrentMember())).collect(Collectors.toList()));
            // On defend
            if (turnCount >= 1 && getCurrentMember().stats.get(ENERGY) > 0 && !getCurrentMember().hasData(Silence.class))
                output.add(getCurrentMember().defend());
            // Check buffs
            getCurrentMember().getBuffs().stream().filter(b -> b.turn() == 0).forEach(buff -> {
                output.add(Emote.INFO + "**" + getCurrentMember().getUsername() + "**'s " + buff.getName() + " has expired.");
                getCurrentMember().data.remove(buff);
            });
        }

        // Start next turn
        turnIndex++;
        // Reset turn index at max
        if (turnIndex >= members.size()) {
            turnIndex = 0;
            // Start game once all players have picked
            if (gameState == PICKING && members.stream().allMatch(Member::hasUnit))
                gameState = PLAYING;
        }

        if (gameState == PICKING) {
            // First pick message
            if (turnIndex == 0) {
                String playerList = getPlayers().stream().map(Player::getUsername).collect(Collectors.joining(", "));
                output.add(Emote.ATTACK + "Welcome to **" + mode.getName() + "**! (" + playerList+ ")");
            }
            output.add("[**" + getCurrentMember().getMention() + ", pick your unit!**]");
            output.add("Check " + instance.getUnitsChannel().getMention() + " to view units, then pick with one with `"
                    + commands.getPrefix() + GameCommand.PICK.getAliases()[0] + "`.");
        } else if (gameState == PLAYING) {
            Member member = getCurrentMember();
            member.heal(member.stats.get(HEALTH_PER_TURN) * (member.defensive ? 2 : 1), null, false);
            member.stats.add(GOLD, mode.handleGold(125 + turnCount));
            member.stats.add(GOLD, member.stats.get(GOLD_PER_TURN));
            member.stats.put(ENERGY, member.getUnit().getStats().get(ENERGY));
            member.stats.add(ENERGY, member.stats.get(ENERGY_PER_TURN));
            member.stats.put(SHIELD, 0);
            member.defensive = false;

            turnCount++;

            output.add("[**" + member.getMention() + ", it's your turn!**]");
            output.add("Open this channel's description to review your stats and items.");

            // On turn start
            output.addAll(member.data.stream().map(e -> e.onTurnStart(member)).collect(Collectors.toList()));
            // Low health warning
            if (member.stats.get(HEALTH) < member.stats.get(MAX_HEALTH) * 0.2f)
                output.add(Emote.WARN + "**" + member.getUsername() + "** is critically low on health.");
        }

        channel.createMessage(Util.joinNonEmpty(output)).block();
        setTopic(getCurrentMember());
    }

    public void setTopic(Member member) {
        channel.edit(c -> c.setTopic(getTopic(member))).block();
    }

    public String getTopic(Member member) {
        if (gameState == PICKING)
            return member.getUsername() + " is picking their unit.";
        else {
            List<String> output = new ArrayList<>();
            output.add(member.unit.getName() + " " + member.getMention() + " (" + turnCount + ") -\n");
            output.add("Gold: **" + Util.comma(member.stats.getInt(GOLD)) + "**");
            output.add("Health: **" + member.stats.getInt(HEALTH) + " / " + member.stats.getInt(MAX_HEALTH)
                    + "** (+**" + member.stats.getInt(HEALTH_PER_TURN) + "**/t)");
            output.add("Energy: **" + member.stats.getInt(ENERGY) + "**");
            output.addAll(Arrays.asList(member.unit.getTopic()));
            output.add("Items: **" + member.getItems() + "**");
            return Util.joinNonEmpty(output);
        }
    }

    public Guild getGuild() {
        return channel.getGuild().block();
    }

    public TextChannel getChannel() {
        return channel;
    }

    public CommandListener getCommandListener() {
        return commands;
    }

    public GameMode getMode() {
        return mode;
    }

    public Member getMember(User user) {
        return members.stream()
                .filter(m -> m.getUser().equals(user))
                .findAny().orElse(null);
    }

    public Member getCurrentMember() {
        return members.get(turnIndex);
    }

    public Member getRandomTarget(Member exclude) {
        List<Member> targets = getAlive().stream().filter(m -> !m.equals(exclude)).collect(Collectors.toList());
        return targets.get(Util.RANDOM.nextInt(targets.size()));
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<User> getUsers() {
        return members.stream().map(Member::getUser).collect(Collectors.toList());
    }

    public List<Player> getPlayers() {
        return members.stream().map(Member::getPlayer).collect(Collectors.toList());
    }

    public List<Member> getAlive() {
        return members.stream().filter(Member::isAlive).collect(Collectors.toList());
    }

    public List<Member> getDead() {
        return members.stream().filter(m -> !m.alive).collect(Collectors.toList());
    }

    public List<GameAction> getActions() {
        return actions;
    }

    public LocalDateTime getLastAction() {
        return lastAction;
    }

    public void setLastAction(LocalDateTime lastAction) {
        this.lastAction = lastAction;
    }

    public int getAfkNotifier() {
        return afkNotifier;
    }

    public void setAfkNotifier(int afkNotifier) {
        this.afkNotifier = afkNotifier;
    }

    public void notifyAfk() {
        afkNotifier++;
        if (afkNotifier == 4)
            channel.createMessage(Emote.WARN + getCurrentMember().getMention() + ", you have around **4** minutes " +
                    "to make an action, otherwise you will **forfeit due to AFKing**.").block();
        else if (afkNotifier >= 8)
            channel.createMessage(getCurrentMember().lose()).block();
    }

    public int getGameState() {
        return gameState;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public class AttackAction implements GameAction {
        private final Member target;

        public AttackAction(Member target) {
            this.target = target;
        }

        public Member getTarget() {
            return target;
        }

        @Override
        public String act(Member actor) {
            return actor.damage(actor.basicAttack(target), Emote.ATTACK);
        }

        @Override
        public int getEnergy() {
            return 50;
        }
    }

    public class BuyAction implements GameAction {
        private final Item item;
        private final int cost;

        public BuyAction(Item item, int cost) {
            this.item = item;
            this.cost = cost;
        }

        @Override
        public String act(Member actor) {
            List<String> output = new ArrayList<>();
            actor.stats.sub(GOLD, cost);
            actor.data.add(item);
            for (Item i : item.getBuild())
                actor.data.remove(i);
            actor.updateStats();

            if (item.getStats().get(MAX_HEALTH) > 0 && !actor.itemHeals.contains(item)) {
                output.add(actor.heal(item.getStats().get(MAX_HEALTH) / 2, item.getName()));
                actor.itemHeals.add(item);
            }

            output.add(0, Emote.BUY + "**" + actor.getUsername() + "** purchased a(n) **"
                    + item.getName() + "** for **" + cost + "** gold.");

            return Util.joinNonEmpty(output);
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }

    public class UseAction implements GameAction {
        private final Item item;

        public UseAction(Item item) {
            this.item = item;
        }

        @Override
        public String act(Member actor) {
            if (item.getCooldown() != null) item.getCooldown().start();
            String output = Emote.USE + "**" + actor.getUsername() + "** used a(n) **" + item.getName() + "**.\n" + item.onUse(actor);
            if (item.removeOnUse()) actor.data.remove(item);
            actor.updateStats();
            return output;
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }

    public class SellAction implements GameAction {
        private final Item item;

        public SellAction(Item item) {
            this.item = item;
        }

        @Override
        public String act(Member actor) {
            int gold = Math.round(item.getCost() * 0.6f);
            String output = Emote.BUY + "**" + actor.getUsername() + "** sold a(n) **" + item.getName() + "** for **" + gold + "** gold.";
            actor.stats.add(GOLD, gold);
            actor.data.remove(item);
            actor.updateStats();
            return output;
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }

    public class Member {
        private Player player;
        private Unit unit;
        private boolean alive = true;
        private boolean defensive = false;

        private List<GameObject> data = new ArrayList<>();
        private List<Item> itemHeals = new ArrayList<>();

        private ChanceBag critBag = new ChanceBag();

        private Stats stats = new Stats();

        public Member(Player player) {
            this.player = player;
        }

        public Member(Member other) {
            this.player = other.player;
            this.unit = other.unit;
            this.alive = other.alive;
            this.defensive = other.defensive;
            this.data = other.data;
            this.itemHeals = other.itemHeals;
            this.critBag = other.critBag;
            this.stats = other.stats;
        }

        public Game getGame() {
            return Game.this;
        }

        public Player getPlayer() {
            return player;
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

        public boolean isAlive() {
            return alive;
        }

        public void setAlive(boolean alive) {
            this.alive = alive;
        }

        public boolean isDefensive() {
            return defensive;
        }

        public void setDefensive(boolean defensive) {
            this.defensive = defensive;
        }

        public List<GameObject> getData() {
            return data;
        }

        public GameObject getData(Class<?> clazz) {
            return data.stream().filter(o -> o.getClass().equals(clazz)).findAny().orElse(null);
        }

        public boolean hasData(Class<?> clazz) {
            return getData(clazz) != null;
        }

        public Stats getStats() {
            return stats;
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

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
            data.clear();
            data.add(unit);
            updateStats();

            stats.put(HEALTH, stats.get(MAX_HEALTH));
            stats.put(GOLD, mode.handleGold(175 + (100 * getAlive().indexOf(this))));

            getCommandListener().getCommands().addAll(Arrays.asList(unit.getCommands()));

            if (unit instanceof Berserker)
                ((Berserker) unit).getRage().setCur(getAlive().indexOf(this));
        }

        public boolean hasUnit() {
            return getUnit() != null;
        }

        public void updateStats() {
            data.removeAll(getEffects());

            float health = unit.getStats().get(HEALTH);
            int gold = unit.getStats().getInt(GOLD);
            int energy = unit.getStats().getInt(ENERGY);

            stats.put(unit.getStats());
            stats.put(HEALTH, health);
            stats.put(GOLD, gold);
            stats.put(ENERGY, energy);

            for (Item item : getItems()) {
                stats.add(item.getStats());
                for (Effect effect : item.getEffects()) {
                    if (!data.contains(effect))
                        data.add(effect);
                    else {
                        Effect oldEffect = (Effect) data.get(data.indexOf(effect));
                        if (effect.getPower() > oldEffect.getPower()) {
                            data.remove(oldEffect);
                            data.add(effect);
                        }
                    }
                }
            }

            for (Effect effect : getEffects()) stats.add(effect.getStats(this));

            critBag.setChance(stats.get(Stats.CRIT_CHANCE));
            critBag.setInfluence(0.5f);
        }

        public void act(GameAction action) {
            if (stats.get(ENERGY) < action.getEnergy())
                Util.sendFailure(channel, "You do not have **" + action.getEnergy() + "** energy.");
            else {
                channel.createMessage(action.execute(this)).block();
                Game.this.actions.add(action);
                stats.sub(ENERGY, action.getEnergy());
                if (stats.get(ENERGY) <= 0) nextTurn();
                else setTopic(this);
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
                if (buff.getPower() > oldBuff.getPower()) {
                    data.remove(oldBuff);
                    data.add(buff);
                    return Emote.BLEED + "**" + buff.getSource().getUsername() + "** applied **" + buff.getName() + "** for **" + buff.getTurns() + "** turn(s)!";
                }
                return "";
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
            stats.add(Stats.HEALTH, amount);
            if (message) return Emote.HEAL + "**" + getUsername() + "** healed by **" + Math.round(amount) + "**! [**"
                    + stats.getInt(Stats.HEALTH) + " / " + stats.getInt(Stats.MAX_HEALTH) + "**]"
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
            return "";
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

        public DamageEvent basicAttack(Member target) {
            DamageEvent event = new DamageEvent(Game.this, this, target);
            event.damage = stats.get(Stats.DAMAGE);
            event.actor.stats.add(Stats.GOLD, mode.handleGold(Math.round(Util.nextInt(20, 30) + (turnCount * 0.5f))));

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

            event = mode.handleDamage(event);

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
                event.target.stats.sub(Stats.HEALTH, event.total());
                event.output.add(0, Util.damageText(event, event.actor.getUsername(), event.target.getUsername(), emote, source));
                if (event.target.stats.get(Stats.HEALTH) <= 0)
                    event.output.add(event.target.lose());
            }

            return Util.joinNonEmpty(event.output);
        }

        public String win() {
            instance.endGame(Game.this);
            return Emote.TROPHY + getUser().getMention() + ", you have won the game!\n";
        }

        public String lose() {
            List<String> output = new ArrayList<>();
            output.add(Emote.SKULL + getUser().getMention() + " has been slain and removed from the game!");

            alive = false;

            if (getAlive().size() == 1) {
                gameState = 2;
                output.add(getAlive().get(0).win());
            } else if (getCurrentMember().equals(this))
                nextTurn();

            return Util.joinNonEmpty(output);
        }

        @Override
        public String toString() {
            return getPlayer().toString();
        }
    }
}
