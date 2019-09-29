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
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.ChanceBag;
import com.oopsjpeg.enigma.util.CommandCenter;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class Game {
    private final TextChannel channel;
    private final GameMode mode;
    private final List<Member> members;
    private final CommandCenter commands;

    private LocalDateTime lastAction = LocalDateTime.now();
    private int notifyAfk = 0;

    private int gameState = 0;
    private int turnCount = 0;
    private int curTurn = 0;
    private Member curMember;

    public Game(Guild guild, GameMode mode, List<Player> players) {
        channel = guild.createTextChannel("game").complete();
        commands = new CommandCenter(Enigma.PREFIX_GAME);

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
        commands.add(new SlashCommand());
        commands.add(new StatsCommand());
        commands.add(new UseCommand());
        Enigma.getClient().addEventListener(commands);

        channel.getManager().putPermissionOverride(guild.getPublicRole(),
                EnumSet.noneOf(Permission.class),
                EnumSet.of(Permission.MESSAGE_READ)).complete();

        for (Player player : players)
            channel.getManager().putPermissionOverride(guild.getMember(player.getUser()),
                    EnumSet.of(Permission.MESSAGE_READ),
                    EnumSet.noneOf(Permission.class)).complete();

        this.mode = mode;
        members = players.stream().map(Member::new).collect(Collectors.toList());
        nextTurn();
    }

    public void nextTurn() {
        List<String> output = new ArrayList<>();

        if (curTurn >= getAlive().size()) {
            curTurn = 0;
            if (gameState == 0) gameState = 1;
        }

        if (turnCount >= 1 && gameState == 1 && curMember.stats.get(Stats.ENERGY) > 0) {
            curMember.defend = 1;
            output.add(Emote.SHIELD + "**" + curMember.getName() + "** is defending!");
            output.add(curMember.unit.onDefend(curMember));
        }

        if (gameState == 0) {
            curMember = getAlive().get(curTurn);
            if (curTurn == 0) {
                channel.sendMessage(Emote.ATTACK + "Welcome to **" + mode.getName() + "**! ("
                        + getPlayers().stream().map(p -> p.getUser().getName()).collect(Collectors.joining(", ")) + ")\n\n"
                        + "[**" + curMember + ", you have first pick!**]\n"
                        + "Check " + Enigma.getUnitsChannel().getAsMention() + " to view available units.").complete();
            } else {
                channel.sendMessage("[**" + curMember + ", you have next pick!**]").complete();
            }
        } else if (gameState == 1) {
            output.addAll(curMember.data.stream()
                    .map(e -> e.onTurnEnd(curMember))
                    .collect(Collectors.toList()));

            curMember.getBuffs().forEach(buff -> {
                if (buff.turn() == 0) {
                    output.add(Emote.INFO + "**" + curMember.getName() + "'s " + buff.getName() + "** has expired.");
                    curMember.data.remove(buff);
                }
            });

            curMember = getAlive().get(curTurn);

            curMember.stats.add(Stats.HP, curMember.perTurn.get(Stats.HP) * (1 + curMember.defend));
            curMember.stats.add(Stats.GOLD, Math.round(curMember.perTurn.get(Stats.GOLD) + (turnCount - 1)));
            curMember.stats.put(Stats.ENERGY, curMember.unit.getStats().get(Stats.ENERGY));
            curMember.stats.add(Stats.ENERGY, curMember.perTurn.get(Stats.ENERGY));
            curMember.stats.put(Stats.SHIELD, 0);
            curMember.defend = 0;

            if (turnCount == 0) {
                output.add("[**" + curMember + ", you have the first turn!**]\n"
                        + "Open the channel's description to review your statistics.\n"
                        + "Check " + Enigma.getItemsChannel().getAsMention() + " to view purchasable items.");
            } else {
                output.add("[**" + curMember + ", it's your turn!**]\n"
                        + "Open the channel's description to review your statistics.");
            }

            output.addAll(curMember.data.stream()
                    .map(e -> e.onTurnStart(curMember))
                    .collect(Collectors.toList()));

            output.removeAll(Arrays.asList(null, ""));

            channel.sendMessage(String.join("\n", output)).complete();

            turnCount++;
        }

        setTopic(curMember);
        curTurn++;
    }

    public void setTopic(Member member) {
        if (gameState == 0) {
            channel.getManager().setTopic(member + " is picking their unit.").complete();
        } else {
            channel.getManager().setTopic(member.unit.getName() + " " + member + " (" + turnCount + ") - \n\n"
                    + "Gold: **" + member.stats.getInt(Stats.GOLD) + "**\n"
                    + "Health: **" + member.stats.getInt(Stats.HP) + " / " + member.stats.getInt(Stats.MAX_HP)
                    + "** (+**" + member.perTurn.getInt(Stats.HP) + "**/t)\n"
                    + "Energy: **" + member.stats.getInt(Stats.ENERGY) + "**\n"
                    + (member.unit instanceof Warrior
                    ? "Attack: **" + ((Warrior) member.unit).getBonus() + " / 3**\n" : "")
                    + (member.unit instanceof Berserker
                    ? "Rage: **" + ((Berserker) member.unit).getRage() + " / 5**\n" : "")
                    + (member.unit instanceof Duelist
                    ? "Bonus: **" + ((Duelist) member.unit).getBonus() + " / " + Duelist.BONUS_MAX + "**\n" : "")
                    + (member.unit instanceof Assassin
                    ? "Slash: **" + ((Assassin) member.unit).getSlashCount() + " / " + Assassin.SLASH_STACK_MAX + "**\n"
                    + "Potency: **" + Math.round(((Assassin) member.unit).getPotency()) + "**\n" : "")
                    + "Items: **" + member.getItems() + "**\n").complete();
        }
    }

    public TextChannel getChannel() {
        return channel;
    }

    public CommandCenter getCommands() {
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

    public LocalDateTime getLastAction() {
        return lastAction;
    }

    public void notifyAfk() {
        notifyAfk++;
        if (notifyAfk == 4)
            channel.sendMessage(Emote.WARN + curMember + ", you have around **4** minutes " +
                    "to make an action, otherwise you will **forfeit due to AFKing**.").complete();
        else if (notifyAfk >= 8)
            channel.sendMessage(curMember.lose()).complete();
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
                Util.sendError(channel, "You cannot attack while silenced.");
            else {
                channel.sendMessage(actor.damage(actor.basicAttack(target), Emote.ATTACK, "damaged")).complete();
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
                Util.sendError(channel, "You need **" + (cost - actor.stats.getInt(Stats.GOLD))
                        + "** more gold for a(n) **" + item.getName() + "**.");
            else if (build.size() >= 6)
                Util.sendError(channel, "You do not have enough inventory space for a(n) **" + item.getName() + "**..");
            else {
                List<String> output = new ArrayList<>();
                actor.stats.sub(Stats.GOLD, cost);
                actor.data.add(item);
                actor.data.removeAll(Arrays.asList(item.getBuild()));
                actor.updateStats();

                if (item.getStats().get(Stats.MAX_HP) > 0 && !actor.shields.contains(item)) {
                    output.add(actor.shield(item.getStats().get(Stats.MAX_HP)));
                    actor.shields.add(item);
                }

                output.add(0, Emote.BUY + "**" + actor.getName() + "** purchased a(n) **"
                        + item.getName() + "** for **" + cost + "** gold.");

                channel.sendMessage(String.join("\n", output)).complete();
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
                Util.sendError(channel, "You don't have a(n) **" + item.getName() + "**.");
            else if (!item.canUse(actor))
                Util.sendError(channel, "**" + item.getName() + "** can't be used.");
            else {
                channel.sendMessage(Emote.USE + "**" + actor.getName() + "** used a(n) **"
                        + item.getName() + "**.\n" + item.onUse(actor)).complete();
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

    public class BashAction extends Action {
        private final Member target;

        public BashAction(Member target) {
            this.target = target;
        }

        @Override
        public boolean act(Member actor) {
            if (!(actor.unit instanceof Warrior))
                Util.sendError(channel, "You are not playing **Warrior**.");
            else if (actor.hasData(Silence.class))
                Util.sendError(channel, "You cannot **Bash** while silenced.");
            else {
                Warrior wu = (Warrior) actor.unit;
                if (wu.getBash())
                    Util.sendError(channel, "You can only use **Bash** once per turn.");
                else {
                    wu.setBash(true);
                    wu.bonus();

                    DamageEvent event = new DamageEvent(Game.this, actor, target);

                    event.damage = actor.stats.get(Stats.DAMAGE) * 0.5f * (1 + actor.stats.get(Stats.ABILITY_POWER));
                    if (event.target.stats.get(Stats.SHIELD) > 0)
                        event.target.stats.put(Stats.SHIELD, 0.01f);

                    channel.sendMessage(actor.damage(event, Emote.KNIFE, "bashed")).complete();
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
                Util.sendError(channel, "You are not playing **Berserker**.");
            else if (actor.hasData(Silence.class))
                Util.sendError(channel, "You cannot **Rage** while silenced.");
            else {
                Berserker berserk = (Berserker) actor.unit;
                berserk.setBonus(0.04f * berserk.getRage() * (1 + actor.stats.get(Stats.ABILITY_POWER)));

                if (berserk.getRage() == 5) actor.stats.add(Stats.ENERGY, 100);

                channel.sendMessage(Emote.RAGE + "**" + actor.getName() + "** has gained **"
                        + Math.round(berserk.getBonus() * 100) + "%** bonus damage "
                        + (berserk.getRage() == 5 ? "and **100** energy " : "") + "this turn!").complete();

                berserk.setRage(0);

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
                Util.sendError(channel, "You are not playing **Assassin**.");
            else if (actor.hasData(Silence.class))
                Util.sendError(channel, "You cannot **Slash** while silenced.");
            else {
                Assassin au = (Assassin) actor.unit;
                if (au.getSlashed())
                    Util.sendError(channel, "You can only use **Slash** once per turn.");
                else {
                    au.setSlashed(true);

                    DamageEvent event = new DamageEvent(Game.this, actor, target);
                    event.damage = actor.stats.get(Stats.DAMAGE) * Assassin.SLASH_DAMAGE * (1 + actor.stats.get(Stats.ABILITY_POWER));
                    event = event.actor.hit(event);
                    event = event.actor.crit(event);

                    if (au.slashCount() >= 4) {
                        event.damage += au.getPotency();
                        event.output.add(target.buff(new Silence(actor, Assassin.SILENCE_TURNS)));
                        au.setSlashCount(0);
                        au.setPotencyTurn(0);
                        au.setPotency(0);
                    }

                    channel.sendMessage(actor.damage(event, Emote.KNIFE, "slashed")).complete();
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
                Util.sendError(channel, "You are not playing **Duelist**.");
            else if (actor.hasData(Silence.class))
                Util.sendError(channel, "You cannot **Crush** while silenced.");
            else {
                Duelist du = (Duelist) actor.unit;
                if (!du.canCrush())
                    Util.sendError(channel, "**Crush** is on cooldown.");
                else {
                    du.setCrush(Duelist.CRUSH_COOLDOWN);
                    channel.sendMessage(target.buff(new Weaken(actor, Duelist.CRUSH_TURNS, Duelist.CRUSH_POWER))).complete();
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
                Util.sendError(channel, "You are not playing **Gunslinger**.");
            else if (actor.hasData(Silence.class))
                Util.sendError(channel, "You cannot **Barrage** while silenced.");
            else {
                Gunslinger gu = (Gunslinger) actor.unit;
                if (!gu.canBarrage())
                    Util.sendError(channel, "**Barrage** is on cooldown.");
                else {
                    gu.setBarrage(Gunslinger.BARRAGE_COOLDOWN);

                    List<String> output = new ArrayList<>();
                    for (int i = 0; i < Gunslinger.BARRAGE_SHOTS; i++)
                        if (target.isAlive()) {
                            DamageEvent event = new DamageEvent(Game.this, actor, target);
                            event.damage = actor.stats.get(Stats.DAMAGE) * Gunslinger.BARRAGE_DAMAGE * (1 + (actor.stats.get(Stats.ABILITY_POWER) * Gunslinger.BARRAGE_AP));
                            actor.crit(event);
                            actor.hit(event);
                            output.add(actor.damage(event, Emote.GUN, "shot"));
                        }
                    output.add(0, Emote.ATTACK + "**" + actor.getName() + "** used **Barrage**!");

                    channel.sendMessage(String.join("\n", output)).complete();
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

        public String getName() {
            return getUser().getName();
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
            stats.put(Stats.GOLD, 300 - (perTurn.get(Stats.GOLD) * (1 - getAlive().indexOf(this))));

            if (unit instanceof Berserker)
                ((Berserker) unit).setRage(getAlive().indexOf(this));
        }

        public void updateStats() {
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
                Util.sendError(channel, "You do not have **" + action.getEnergy() + "** energy.");
            else if (action.execute(this)) {
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
                    return Emote.DEBUFF + "**" + buff.getSource() + "** applied **" + buff.getName() + "** for **" + buff.getTurns() + "** turn(s)!";
                }
                return "";
            }

            data.add(buff);
            return Emote.DEBUFF + "**" + buff.getSource() + "** applied **" + buff.getName() + "** for **" + buff.getTurns() + "** turn(s)!";
        }

        public String shield(float amount) {
            stats.add(Stats.SHIELD, amount);
            return Emote.HEAL + "**" + getName() + "** shielded by **" + Math.round(amount)
                    + "**! [**" + stats.getInt(Stats.SHIELD) + "**]";
        }

        public String heal(float amount) {
            return heal(amount, "");
        }

        public String heal(float amount, String source) {
            amount *= 1 - (hasData(Wound.class) ? ((Wound) getData(Wound.class)).getPower() : 0);
            stats.add(Stats.HP, amount);
            return Emote.HEAL + "**" + getName() + "** healed by **" + Math.round(amount) + "**! [**"
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
            event.actor.stats.add(Stats.GOLD, Util.nextInt(15, 25) + (curTurn * 0.5f));

            for (GameObject o : event.actor.data) event = o.onBasicAttack(event);
            for (GameObject o : event.target.data) event = o.wasBasicAttacked(event);

            event = hit(event);
            event = crit(event);

            return event;
        }

        public String damage(DamageEvent event, String emote, String action) {
            return damage(event, event.actor.getName(), emote, action);
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
                float shieldDmg = Util.limit(event.target.stats.get(Stats.SHIELD), 0, event.damage);
                event.target.stats.sub(Stats.SHIELD, shieldDmg);

                if (event.target.stats.get(Stats.SHIELD) > 0)
                    event.output.add(0, Util.damageText(event, action, event.target.getName() + "'s Shield", emote, action));
                else {
                    event.damage -= shieldDmg;
                    event.output.add(0, Emote.SHIELD + "**" + actor + "** destroyed **" + event.target.getName() + "'s Shield**!");
                }
            }

            if (event.target.stats.get(Stats.SHIELD) <= 0) {
                event.target.stats.sub(Stats.HP, event.damage + event.bonus);
                event.output.add(0, Util.damageText(event, actor, event.target.getName(), emote, action));
                if (event.target.stats.get(Stats.HP) <= 0)
                    event.output.add(event.target.lose());
            }

            event.output.removeAll(Arrays.asList(null, ""));

            return String.join("\n", event.output);
        }

        public String win() {
            Enigma.endGame(Game.this);
            return Emote.TROPHY + getUser().getAsMention() + ", you have won the game!\n";
        }

        public String lose() {
            List<String> output = new ArrayList<>();
            output.add(Emote.SKULL + getUser().getAsMention() + " has been slain and removed from the game!");

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
            return getUser().getAsMention();
        }
    }
}
