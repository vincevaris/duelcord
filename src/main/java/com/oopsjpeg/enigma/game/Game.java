package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.commands.game.*;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.game.buff.Weaken;
import com.oopsjpeg.enigma.game.buff.Wound;
import com.oopsjpeg.enigma.game.obj.Buff;
import com.oopsjpeg.enigma.game.obj.Effect;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.game.unit.*;
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
import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private final TextChannel channel;
    private final GameMode mode;
    private final List<Member> members;
    private final CommandListener commands;

    private List<Action> actions = new ArrayList<>();
    private LocalDateTime lastAction = LocalDateTime.now();
    private int notifyAfk = 0;

    private int gameState = 0;
    private int turnCount = 0;
    private int curTurn = 0;
    private Member curMember;

    public Game(Guild guild, GameMode mode, List<Player> players) {
        channel = guild.createTextChannel(c -> c.setName("game")).block();

        commands = new CommandListener(Enigma.getInstance().getSettings().get(Settings.GAME_PREFIX));
        commands.add(new AttackCommand());
        commands.add(new BarrageCommand());
        commands.add(new BashCommand());
        commands.add(new BuyCommand());
        commands.add(new CrushCommand());
        commands.add(new EndCommand());
        commands.add(new ForfeitCommand());
        commands.add(new PickCommand());
        commands.add(new RageCommand());
        commands.add(new RefreshCommand());
        commands.add(new SellCommand());
        commands.add(new SlashCommand());
        commands.add(new StatsCommand());
        commands.add(new UseCommand());
        Enigma.getInstance().addListener(commands);

        Snowflake roleId = guild.getEveryoneRole().block().getId();
        channel.addRoleOverwrite(roleId, PermissionOverwrite.forRole(roleId,
                PermissionSet.none(), PermissionSet.of(Permission.VIEW_CHANNEL))).block();
        players.forEach(p -> channel.addMemberOverwrite(Snowflake.of(p.getId()), PermissionOverwrite.forMember(Snowflake.of(p.getId()),
                PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none())).block());

        this.mode = mode;
        members = players.stream().map(Member::new).collect(Collectors.toList());
        Collections.shuffle(members);
        nextTurn();
    }

    public void nextTurn() {
        List<String> output = new ArrayList<>();

        if (curTurn >= getAlive().size()) {
            curTurn = 0;
            if (gameState == 0) gameState = 1;
        }

        if (turnCount >= 1 && gameState == 1 && curMember.stats.get(Stats.ENERGY) > 0 && !curMember.hasData(Silence.class)) {
            curMember.defend = 1;
            output.add(Emote.SHIELD + "**" + curMember.getUsername() + "** is defending (**20%** damage reduction, **"
                    + (curMember.perTurn.getInt(Stats.HP) * 2) + "** HP/t)!");
            output.add(curMember.unit.onDefend(curMember));
        }

        if (gameState == 0) {
            curMember = getAlive().get(curTurn);
            if (curTurn == 0) {
                channel.createMessage(Emote.ATTACK + "Welcome to **" + mode.getName() + "**! ("
                        + getPlayers().stream().map(Player::getUsername).collect(Collectors.joining(", ")) + ")"
                        + "\n\n[**" + curMember.getMention() + ", you have first pick!**]"
                        + "\nCheck " + Enigma.getInstance().getUnitsChannel().getMention() + " to view available units, then pick with `>pick`.").block();
            } else {
                channel.createMessage("[**" + curMember.getMention() + ", you have next pick!**]"
                        + "\nCheck" + Enigma.getInstance().getUnitsChannel().getMention() + " to view available units, then pick with `>pick`.").block();
            }
        } else if (gameState == 1) {
            output.addAll(curMember.data.stream()
                    .map(e -> e.onTurnEnd(curMember))
                    .collect(Collectors.toList()));

            curMember.getBuffs().forEach(buff -> {
                if (buff.turn() == 0) {
                    output.add(Emote.INFO + "**" + curMember.getUsername() + "'s " + buff.getName() + "** has expired.");
                    curMember.data.remove(buff);
                }
            });

            curMember = getAlive().get(curTurn);

            curMember.stats.add(Stats.HP, curMember.perTurn.get(Stats.HP) * (1 + curMember.defend));
            curMember.stats.add(Stats.GOLD, Math.round(125 + turnCount));
            curMember.stats.put(Stats.ENERGY, curMember.unit.getStats().get(Stats.ENERGY));
            curMember.stats.add(Stats.ENERGY, curMember.perTurn.get(Stats.ENERGY));
            curMember.stats.put(Stats.SHIELD, 0);
            curMember.defend = 0;

            if (turnCount == 0) {
                output.add("[**" + curMember.getMention() + ", you have the first turn!**]\n"
                        + "Open the channel's description to review your statistics.\n"
                        + "Check " + Enigma.getInstance().getItemsChannel().getMention() + " to view purchasable items.");
            } else {
                output.add("[**" + curMember.getMention() + ", it's your turn!**]\n"
                        + "Open the channel's description to review your statistics.");
            }

            output.addAll(curMember.data.stream()
                    .map(e -> e.onTurnStart(curMember))
                    .collect(Collectors.toList()));
            if (curMember.stats.get(Stats.HP) < curMember.stats.get(Stats.MAX_HP) * 0.2f)
                output.add(Emote.WARN + "**" + curMember.getUsername() + "** is critically low on health.");

            output.removeAll(Arrays.asList(null, ""));

            channel.createMessage(String.join("\n", output)).block();

            turnCount++;
        }

        setTopic(curMember);
        curTurn++;
    }

    public void setTopic(Member member) {
        if (gameState == 0) {
            channel.edit(c -> c.setTopic(member.getUsername() + " is picking their unit.")).block();
        } else {
            channel.edit(c -> c.setTopic(member.unit.getName() + " " + member.getMention() + " (" + turnCount + ") - \n\n"
                    + "Gold: **" + member.stats.getInt(Stats.GOLD) + "**\n"
                    + "Health: **" + member.stats.getInt(Stats.HP) + " / " + member.stats.getInt(Stats.MAX_HP)
                    + "** (+**" + member.perTurn.getInt(Stats.HP) + "**/t)\n"
                    + "Energy: **" + member.stats.getInt(Stats.ENERGY) + "**\n"
                    + (member.unit instanceof Warrior
                    ? "Attack: **" + ((Warrior) member.unit).getBonus().getCur() + " / 3**\n" : "")
                    + (member.unit instanceof Berserker
                    ? "Rage: **" + ((Berserker) member.unit).getRage().getCur() + " / 5**\n" : "")
                    + (member.unit instanceof Duelist
                    ? "Bonus: **" + ((Duelist) member.unit).getBonus().getCur() + " / " + Duelist.BONUS_MAX + "**\n" : "")
                    + (member.unit instanceof Assassin
                    ? "Slash: **" + ((Assassin) member.unit).getSlash().getCur() + " / " + Assassin.SLASH_MAX + "**\n"
                    + "Potency: **" + Math.round(((Assassin) member.unit).getPotencyTotal()) + "**\n" : "")
                    + "Items: **" + member.getItems() + "**\n")).block();
        }
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
        return curMember;
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

    public List<Action> getActions() {
        return actions;
    }

    public LocalDateTime getLastAction() {
        return lastAction;
    }

    public void notifyAfk() {
        notifyAfk++;
        if (notifyAfk == 4)
            channel.createMessage(Emote.WARN + curMember + ", you have around **4** minutes " +
                    "to make an action, otherwise you will **forfeit due to AFKing**.").block();
        else if (notifyAfk >= 8)
            channel.createMessage(curMember.lose()).block();
    }

    public int getGameState() {
        return gameState;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public abstract class Action {
        public boolean execute(Member actor) {
            lastAction = LocalDateTime.now();
            notifyAfk = 0;
            return act(actor);
        }

        public abstract boolean act(Member actor);

        public abstract int getEnergy();
    }

    public class AttackAction extends Action {
        private final Member target;

        public AttackAction(Member target) {
            this.target = target;
        }

        public Member getTarget() {
            return target;
        }

        @Override
        public boolean act(Member actor) {
            if (actor.hasData(Silence.class))
                Util.sendFailure(channel, "You cannot attack while silenced.");
            else {
                channel.createMessage(actor.damage(actor.basicAttack(target), Emote.ATTACK, "damaged")).block();
                return true;
            }
            return false;
        }

        @Override
        public int getEnergy() {
            return 50;
        }
    }

    public class BuyAction extends Action {
        private final Item item;

        public BuyAction(Item item) {
            this.item = item;
        }

        @Override
        public boolean act(Member actor) {
            int cost = item.getCost();
            List<Item> build = actor.getItems();
            for (Item i : item.getBuild())
                if (build.contains(i)) {
                    cost -= i.getCost();
                    build.remove(i);
                }

            if (actor.stats.get(Stats.GOLD) < cost)
                Util.sendFailure(channel, "You need **" + (cost - actor.stats.getInt(Stats.GOLD))
                        + "** more gold for a(n) **" + item.getName() + "**.");
            else if (build.size() >= 4)
                Util.sendFailure(channel, "You do not have enough inventory space for a(n) **" + item.getName() + "**.");
            else {
                List<String> output = new ArrayList<>();
                actor.stats.sub(Stats.GOLD, cost);
                actor.data.add(item);
                for (Item i : item.getBuild())
                    actor.data.remove(i);
                actor.updateStats();

                if (item.getStats().get(Stats.MAX_HP) > 0 && !actor.shields.contains(item)) {
                    output.add(actor.shield(item.getStats().get(Stats.MAX_HP)));
                    actor.shields.add(item);
                }

                output.add(0, Emote.BUY + "**" + actor.getUsername() + "** purchased a(n) **"
                        + item.getName() + "** for **" + cost + "** gold.");

                channel.createMessage(String.join("\n", output)).block();
                return true;
            }
            return false;
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }

    public class UseAction extends Action {
        private final Item item;

        public UseAction(Item item) {
            this.item = item;
        }

        @Override
        public boolean act(Member actor) {
            if (!actor.data.contains(item))
                Util.sendFailure(channel, "You don't have a(n) **" + item.getName() + "**.");
            else if (!item.canUse(actor))
                Util.sendFailure(channel, "**" + item.getName() + "** can't be used.");
            else if (item.getCooldown() != null && !item.getCooldown().count())
                Util.sendFailure(channel, "**" + item.getName() + "** is on cooldown for **" + item.getCooldown().getCur() + "** more turn(s).");
            else {
                if (item.getCooldown() != null) item.getCooldown().start();
                channel.createMessage(Emote.USE + "**" + actor.getUsername() + "** used a(n) **"
                        + item.getName() + "**.\n" + item.onUse(actor)).block();
                if (item.removeOnUse()) actor.data.remove(item);
                actor.updateStats();
                return true;
            }
            return false;
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }

    public class SellAction extends Action {
        private final Item item;

        public SellAction(Item item) {
            this.item = item;
        }

        @Override
        public boolean act(Member actor) {
            if (!actor.data.contains(item))
                Util.sendFailure(channel, "You don't have a(n) **" + item.getName() + "**.");
            else {
                int gold = Math.round(item.getCost() * 0.6f);
                channel.createMessage(Emote.BUY + "**" + actor.getUsername() + "** sold a(n) **"
                        + item.getName() + "** for **" + gold + "** gold.").block();
                actor.stats.add(Stats.GOLD, gold);
                actor.data.remove(item);
                actor.updateStats();
                return true;
            }
            return false;
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }

    public class BashAction extends Action {
        private final Member target;

        public BashAction(Member target) {
            this.target = target;
        }

        @Override
        public boolean act(Member actor) {
            if (!(actor.unit instanceof Warrior))
                Util.sendFailure(channel, "You are not playing **Warrior**.");
            else if (actor.hasData(Silence.class))
                Util.sendFailure(channel, "You cannot **Bash** while silenced.");
            else {
                Warrior wu = (Warrior) actor.unit;
                if (wu.getBash())
                    Util.sendFailure(channel, "You can only use **Bash** once per turn.");
                else {
                    wu.setBash(true);
                    wu.getBonus().stack();

                    DamageEvent event = new DamageEvent(Game.this, actor, target);

                    event.damage = actor.stats.get(Stats.DAMAGE) * Warrior.BASH_DAMAGE;
                    if (event.target.stats.get(Stats.SHIELD) > 0)
                        event.target.stats.put(Stats.SHIELD, 0.01f);

                    channel.createMessage(actor.damage(event, Emote.KNIFE, "bashed")).block();
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }

    public class RageAction extends Action {
        @Override
        public boolean act(Member actor) {
            if (!(actor.unit instanceof Berserker))
                Util.sendFailure(channel, "You are not playing **Berserker**.");
            else if (actor.hasData(Silence.class))
                Util.sendFailure(channel, "You cannot **Rage** while silenced.");
            else {
                Berserker be = (Berserker) actor.unit;
                float stack = Berserker.BONUS_DAMAGE + (actor.stats.get(Stats.ABILITY_POWER) / (Berserker.BONUS_AP * 100));

                be.setBonus(stack * be.getRage().getCur());

                if (be.getRage().getCur() == Berserker.RAGE_MAX)
                    actor.stats.add(Stats.ENERGY, 100);

                channel.createMessage(Emote.RAGE + "**" + actor.getUsername() + "** has gained **"
                        + Util.percent(be.getBonus()) + "** bonus damage "
                        + (be.getRage().getCur() == Berserker.RAGE_MAX ? "and **100** energy " : "")
                        + "this turn!").block();

                be.getRage().reset();

                return true;
            }
            return false;
        }

        @Override
        public int getEnergy() {
            return 0;
        }
    }

    public class SlashAction extends Action {
        private final Member target;

        public SlashAction(Member target) {
            this.target = target;
        }

        @Override
        public boolean act(Member actor) {
            if (!(actor.unit instanceof Assassin))
                Util.sendFailure(channel, "You are not playing **Assassin**.");
            else if (actor.hasData(Silence.class))
                Util.sendFailure(channel, "You cannot **Slash** while silenced.");
            else {
                Assassin au = (Assassin) actor.unit;
                if (au.getSlashed())
                    Util.sendFailure(channel, "You can only use **Slash** once per turn.");
                else {
                    au.setSlashed(true);

                    DamageEvent event = new DamageEvent(Game.this, actor, target);
                    event.damage = (actor.stats.get(Stats.DAMAGE) * Assassin.SLASH_DAMAGE) + (actor.stats.get(Stats.ABILITY_POWER) * Assassin.SLASH_AP);

                    if (au.getSlash().stack()) {
                        event.damage += au.getPotencyTotal();
                        event.output.add(target.buff(new Silence(actor, Assassin.SILENCE_TURNS)));
                        au.getSlash().reset();
                        au.getPotency().reset();
                        au.setPotencyTotal(0);
                    }

                    event = event.actor.hit(event);
                    event = event.actor.crit(event);

                    channel.createMessage(actor.damage(event, Emote.KNIFE, "slashed")).block();
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }

    public class CrushAction extends Action {
        private final Member target;

        public CrushAction(Member target) {
            this.target = target;
        }

        @Override
        public boolean act(Member actor) {
            if (!(actor.unit instanceof Duelist))
                Util.sendFailure(channel, "You are not playing **Duelist**.");
            else if (actor.hasData(Silence.class))
                Util.sendFailure(channel, "You cannot **Crush** while silenced.");
            else {
                Duelist du = (Duelist) actor.unit;
                if (!du.getCrush().done())
                    Util.sendFailure(channel, "**Crush** is on cooldown for **" + du.getCrush().getCur() + "** more turn(s).");
                else {
                    du.getCrush().start();
                    channel.createMessage(target.buff(new Weaken(actor, Duelist.CRUSH_TURNS, Duelist.CRUSH_POWER))).block();
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getEnergy() {
            return 25;
        }
    }

    public class BarrageAction extends Action {
        private final Member target;

        public BarrageAction(Member target) {
            this.target = target;
        }

        @Override
        public boolean act(Member actor) {
            if (!(actor.unit instanceof Gunslinger))
                Util.sendFailure(channel, "You are not playing **Gunslinger**.");
            else if (actor.hasData(Silence.class))
                Util.sendFailure(channel, "You cannot **Barrage** while silenced.");
            else {
                Gunslinger gu = (Gunslinger) actor.unit;
                if (!gu.getBarrage().done())
                    Util.sendFailure(channel, "**Barrage** is on cooldown for **" + gu.getBarrage().getCur() + "** more turn(s).");
                else {
                    gu.getBarrage().start();

                    List<String> output = new ArrayList<>();
                    for (int i = 0; i < Gunslinger.BARRAGE_SHOTS; i++)
                        if (target.isAlive()) {
                            DamageEvent event = new DamageEvent(Game.this, actor, target);
                            event.damage = (actor.stats.get(Stats.DAMAGE) * Gunslinger.BARRAGE_DAMAGE) + (actor.stats.get(Stats.ABILITY_POWER) * Gunslinger.BARRAGE_AP);
                            actor.crit(event);
                            actor.hit(event);
                            output.add(actor.damage(event, Emote.GUN, "shot"));
                        }
                    output.add(0, Emote.ATTACK + "**" + actor.getUsername() + "** used **Barrage**!");

                    channel.createMessage(String.join("\n", output)).block();
                    return true;
                }
            }
            return false;
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
        private int defend = 0;

        private List<GameObject> data = new ArrayList<>();
        private List<Item> shields = new ArrayList<>();

        private ChanceBag critBag = new ChanceBag();

        private Stats stats = new Stats();
        private Stats perTurn = new Stats();

        public Member(Player player) {
            this.player = player;
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

        public int getDefend() {
            return defend;
        }

        public void setDefend(int defend) {
            this.defend = defend;
        }

        public List<GameObject> getData() {
            return data;
        }

        public GameObject getData(Class clazz) {
            return data.stream().filter(o -> o.getClass().equals(clazz)).findAny().orElse(null);
        }

        public boolean hasData(Class clazz) {
            return getData(clazz) != null;
        }

        public Stats getStats() {
            return stats;
        }

        public Stats getPerTurn() {
            return perTurn;
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

            stats.put(Stats.HP, stats.get(Stats.MAX_HP));
            stats.put(Stats.GOLD, 175 + (100 * getAlive().indexOf(this)));

            if (unit instanceof Berserker)
                ((Berserker) unit).getRage().setCur(getAlive().indexOf(this));
        }

        public void updateStats() {
            data.removeAll(getEffects());
            stats.put(Stats.MAX_HP, unit.getStats().get(Stats.MAX_HP));
            stats.put(Stats.DAMAGE, unit.getStats().get(Stats.DAMAGE));
            stats.put(Stats.ABILITY_POWER, unit.getStats().get(Stats.ABILITY_POWER));
            stats.put(Stats.CRIT_CHANCE, unit.getStats().get(Stats.CRIT_CHANCE));
            stats.put(Stats.CRIT_DAMAGE, unit.getStats().get(Stats.CRIT_DAMAGE));
            stats.put(Stats.LIFE_STEAL, unit.getStats().get(Stats.LIFE_STEAL));
            perTurn.put(Stats.HP, unit.getPerTurn().get(Stats.HP));
            perTurn.put(Stats.GOLD, unit.getPerTurn().get(Stats.GOLD));
            perTurn.put(Stats.ENERGY, unit.getPerTurn().get(Stats.ENERGY));

            for (Item item : getItems()) {
                stats.add(item.getStats());
                perTurn.add(item.getPerTurn());
                for (Effect effect : item.getEffects()) {
                    if (!data.contains(effect)) data.add(effect);
                    else {
                        Effect oldEffect = (Effect) data.get(data.indexOf(effect));
                        if (effect.getPower() > oldEffect.getPower()) {
                            data.remove(oldEffect);
                            data.add(effect);
                        }
                    }
                }
            }

            for (Effect effect : getEffects()) {
                stats.add(effect.getStats());
                perTurn.add(effect.getPerTurn());
            }

            critBag.setChance(stats.get(Stats.CRIT_CHANCE));
            critBag.setInfluence(0.5f);
        }

        public void act(Action action) {
            if (stats.get(Stats.ENERGY) < action.getEnergy())
                Util.sendFailure(channel, "You do not have **" + action.getEnergy() + "** energy.");
            else if (action.execute(this)) {
                Game.this.actions.add(action);
                stats.sub(Stats.ENERGY, action.getEnergy());
                if (stats.get(Stats.ENERGY) <= 0) nextTurn();
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
                    return Emote.DEBUFF + "**" + buff.getSource().getUsername() + "** applied **" + buff.getName() + "** for **" + buff.getTurns() + "** turn(s)!";
                }
                return "";
            }

            data.add(buff);
            return Emote.DEBUFF + "**" + buff.getSource().getUsername() + "** applied **" + buff.getName() + "** for **" + buff.getTurns() + "** turn(s)!";
        }

        public String shield(float amount) {
            stats.add(Stats.SHIELD, amount);
            return Emote.HEAL + "**" + getUsername() + "** shielded by **" + Math.round(amount)
                    + "**! [**" + stats.getInt(Stats.SHIELD) + "**]";
        }

        public String heal(float amount) {
            return heal(amount, "");
        }

        public String heal(float amount, String source) {
            amount *= 1 - (hasData(Wound.class) ? ((Wound) getData(Wound.class)).getPower() : 0);
            stats.add(Stats.HP, amount);
            return Emote.HEAL + "**" + getUsername() + "** healed by **" + Math.round(amount) + "**! [**"
                    + stats.getInt(Stats.HP) + " / " + stats.getInt(Stats.MAX_HP) + "**]" + (source.isEmpty() ? "" : " (" + source + ")");
        }

        public DamageEvent hit(DamageEvent event) {
            for (GameObject o : event.actor.data) event = o.onHit(event);
            for (GameObject o : event.target.data) event = o.wasHit(event);

            // Life steal healing
            if (stats.get(Stats.LIFE_STEAL) > 0)
                event.output.add(heal(Math.round(stats.get(Stats.LIFE_STEAL) * event.damage)));

            return event;
        }

        public DamageEvent crit(DamageEvent event) {
            // Crit checks
            if (event.crit || !event.miss && critBag.get()) {
                // Pseudo RNG crit bag
                event.crit = true;

                for (GameObject o : event.actor.data) event = o.onCrit(event);
                for (GameObject o : event.target.data) event = o.wasCrit(event);
            }

            // Critical strike bonus damage
            if (event.crit) {
                event.critMul += .5f + stats.get(Stats.CRIT_DAMAGE);
                event.damage += event.damage * event.critMul;
            }

            return event;
        }

        public DamageEvent basicAttack(Member target) {
            DamageEvent event = new DamageEvent(Game.this, this, target);
            event.damage = stats.get(Stats.DAMAGE);
            event.actor.stats.add(Stats.GOLD, Util.nextInt(20, 30) + (curTurn * 0.5f));

            for (GameObject o : event.actor.data) event = o.onBasicAttack(event);
            for (GameObject o : event.target.data) event = o.wasBasicAttacked(event);

            event = hit(event);
            event = crit(event);

            return event;
        }

        public String damage(DamageEvent event, String emote, String action) {
            return damage(event, event.actor.getUsername(), emote, action);
        }

        public String damage(DamageEvent event, String actor, String emote, String action) {
            for (GameObject o : event.actor.data) event = o.onDamage(event);
            for (GameObject o : event.target.data) event = o.wasDamaged(event);

            // Defensive stance damage reduction
            if (event.target.defend == 1) {
                event.damage *= 0.8f;
                event.bonus *= 0.8f;
            }

            // Shield damaging
            if (event.target.stats.get(Stats.SHIELD) > 0) {
                // Remove bonus damage first
                float shdBonus = Util.limit(event.bonus, 0, event.target.stats.get(Stats.SHIELD));
                event.bonus -= shdBonus;
                event.target.stats.sub(Stats.SHIELD, shdBonus);

                // Remove main damage after
                if (event.target.stats.get(Stats.SHIELD) > 0) {
                    float shdDamage = Util.limit(event.damage, 0, event.target.stats.get(Stats.SHIELD));
                    event.damage -= shdDamage;
                    event.target.stats.sub(Stats.SHIELD, shdDamage);
                }

                if (event.target.stats.get(Stats.SHIELD) > 0)
                    event.output.add(0, Util.damageText(event, actor, event.target.getUsername() + "'s Shield", emote, action));
                else
                    event.output.add(0, Emote.SHIELD + "**" + actor + "** destroyed **" + event.target.getUsername() + "'s Shield**!");
            }

            if (event.target.stats.get(Stats.SHIELD) <= 0 && event.total() > 0) {
                event.target.stats.sub(Stats.HP, event.total());
                event.output.add(0, Util.damageText(event, actor, event.target.getUsername(), emote, action));
                if (event.target.stats.get(Stats.HP) <= 0)
                    event.output.add(event.target.lose());
            }

            event.output.removeAll(Arrays.asList(null, ""));

            return String.join("\n", event.output);
        }

        public String win() {
            Enigma.getInstance().endGame(Game.this);
            return Emote.TROPHY + getUser().getMention() + ", you have won the game!\n";
        }

        public String lose() {
            List<String> output = new ArrayList<>();
            output.add(Emote.SKULL + getUser().getMention() + " has been slain and removed from the game!");

            alive = false;

            if (getAlive().size() == 1) {
                gameState = 2;
                output.add(getAlive().get(0).win());
            } else if (curMember.equals(this))
                nextTurn();

            return String.join("\n", output);
        }

        @Override
        public String toString() {
            return getPlayer().toString();
        }
    }
}
