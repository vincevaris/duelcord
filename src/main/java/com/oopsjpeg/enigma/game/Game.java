package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.util.ChanceBag;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.effects.LoveOfWar;
import com.oopsjpeg.enigma.game.effects.util.Effect;
import com.oopsjpeg.enigma.game.items.util.Item;
import com.oopsjpeg.enigma.game.units.BerserkerUnit;
import com.oopsjpeg.enigma.game.units.ThiefUnit;
import com.oopsjpeg.enigma.game.units.WarriorUnit;
import com.oopsjpeg.enigma.game.units.util.Unit;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.roboops.framework.RoboopsUtil;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class Game {
	private final IChannel channel;
	private final GameMode mode;
	private final List<Member> members;

	private LocalDateTime lastAction = LocalDateTime.now();
	private int notifyAfk = 0;

	private int gameState = 0;
	private int turnCount = 0;
	private int curTurn = 0;
	private Member curMember;

	public Game(IGuild guild, GameMode mode, List<Player> players) {
		channel = guild.createChannel("game");

		RoboopsUtil.overrideRolePermissions(channel, guild.getEveryoneRole(),
				EnumSet.noneOf(Permissions.class), EnumSet.of(Permissions.READ_MESSAGES));
		for (Player player : players)
			RoboopsUtil.overrideUserPermissions(channel, player.getUser(),
					EnumSet.of(Permissions.READ_MESSAGES), EnumSet.noneOf(Permissions.class));

		this.mode = mode;
		members = players.stream().map(Member::new).collect(Collectors.toList());
		nextTurn();

		Enigma.getClient().getDispatcher().registerListeners(new GameListener());
	}

	public void nextTurn() {
		if (curTurn >= getAlive().size()) {
			curTurn = 0;
			if (gameState == 0) gameState = 1;
		}

		if (turnCount >= 1 && gameState == 1 && curMember.stats.energy > 0) {
			curMember.defend = 1;
			String onDefend = curMember.unit.onDefend();
			if (!onDefend.isEmpty()) RoboopsUtil.sendMessage(channel, onDefend);
		}

		curMember = getAlive().get(curTurn);

		if (gameState == 0) {
			if (curTurn == 0) {
				RoboopsUtil.sendMessage(channel, "Welcome to **" + mode.getName() + "**! ("
						+ getPlayers().stream().map(Player::getName).collect(Collectors.joining(", ")) + ")\n"
						+ curMember + ", you have first pick!\n"
						+ "Check " + Enigma.getUnitsChannel() + " to view available units.");
			} else {
				RoboopsUtil.sendMessage(channel, curMember + ", you have next pick!");
			}
		} else if (gameState == 1) {
			curMember.stats.hp = Math.max(0, Math.min(curMember.stats.maxHp,
					curMember.stats.hp + (curMember.perTurn.hp * curMember.defend)));
			curMember.stats.gold += Math.round(curMember.perTurn.gold + (turnCount * 0.5));
			curMember.stats.energy = curMember.unit.getStats().energy + curMember.perTurn.energy;
			curMember.stats.shield = 0;

			curMember.unit.onTurn();
			curMember.effects.forEach(Effect::onTurn);
			curMember.defend = 0;

			if (turnCount == 0) {
				RoboopsUtil.sendMessage(channel, curMember + ", you have the first turn!\n"
						+ "Open the channel's description to review your statistics.\n"
						+ "Check " + Enigma.getItemsChannel() + " to view purchasable items.");
			} else {
				RoboopsUtil.sendMessage(channel, curMember + ", it's your turn!\n"
						+ "Open the channel's description to review your statistics.");
			}
			turnCount++;
		}

		setTopic(curMember);
		curTurn++;
	}

	public void setTopic(Member member) {
		if (gameState == 0) {
			RoboopsUtil.changeTopic(channel, member.getUser() + " is picking their unit.");
		} else {
			RoboopsUtil.changeTopic(channel, member.unit.getName() + " " + member.getUser() + " (" + turnCount + ") - \n\n"
					+ "Gold: **" + member.stats.gold + "**\n"
					+ "Health: **" + member.stats.hp + " / " + member.stats.maxHp
					+ "** (+**" + member.perTurn.hp + "**/t)\n"
					+ "Energy: **" + member.stats.energy + "**\n"
					+ (member.unit instanceof WarriorUnit
					? "Strike: **" + ((WarriorUnit) member.unit).getBonus() + " / 3**\n" : "")
					+ (member.unit instanceof BerserkerUnit
					? "Rage: **" + ((BerserkerUnit) member.unit).getRage() + " / 6**\n" : "")
					+ "Items: **" + member.items + "**\n");
		}
	}

	public IChannel getChannel() {
		return channel;
	}

	public GameMode getMode() {
		return mode;
	}

	public List<Member> getMembers() {
		return members;
	}

	public List<IUser> getUsers() {
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
			RoboopsUtil.sendMessage(channel, Emote.WARN + curMember.getUser() + ", you have around **3** minutes " +
					"to make an action, otherwise you will **forfeit due to AFKing**.");
		else if (notifyAfk >= 8)
			RoboopsUtil.sendMessage(channel, curMember.lose());
	}

	public int getGameState() {
		return gameState;
	}

	public class GameListener {
		@EventSubscriber
		public void onMessage(MessageReceivedEvent event) {
			IMessage message = event.getMessage();
			String content = message.getContent();
			IUser author = message.getAuthor();

			if (gameState != 2 && getUsers().contains(author) && content.startsWith(Enigma.PREFIX_GAME)) {
				String[] split = content.split(" ");
				String alias = split[0].replaceFirst(Enigma.PREFIX_GAME, "");
				String[] args = Arrays.copyOfRange(split, 1, split.length);

				if (alias.equalsIgnoreCase("refresh") || alias.equalsIgnoreCase("refreshtopic")) {
					RoboopsUtil.deleteMessage(message);
					setTopic(curMember);
				} else if (curMember.equals(author) && alias.equalsIgnoreCase("pick")) {
					RoboopsUtil.deleteMessage(message);
					if (gameState == 1)
						RoboopsUtil.sendMessage(channel, Emote.NO + "You cannot pick a unit after the game has started.");
					else {
						Unit unit = Unit.fromName(String.join(" ", args));
						if (unit == null)
							RoboopsUtil.sendMessage(channel, Emote.NO + "Invalid unit.");
						else {
							curMember.setUnit(unit);
							RoboopsUtil.sendMessage(channel, Emote.YES + " **" + author.getName() + "** has selected **"
									+ unit.getName() + "**.");
							nextTurn();
						}
					}
				} else if (curMember.equals(author) && alias.equalsIgnoreCase("end")) {
					RoboopsUtil.deleteMessage(message);
					if (gameState == 0)
						RoboopsUtil.sendMessage(channel, Emote.NO + "You cannot end your turn until the game has started.");
					else
						nextTurn();
				} else if (curMember.equals(author) && alias.equalsIgnoreCase("attack")) {
					RoboopsUtil.deleteMessage(message);
					if (gameState == 0)
						RoboopsUtil.sendMessage(channel, Emote.NO + "You cannot attack until the game has started.");
					else {
						Member target = getAlive().stream().filter(m -> !m.equals(curMember)).findAny().orElse(null);
						if (target == null)
							RoboopsUtil.sendMessage(channel, Emote.NO + "There is no one to attack.");
						else
							curMember.act(new AttackAction(target));
					}
				} else if (curMember.equals(author) && alias.equalsIgnoreCase("buy")) {
					RoboopsUtil.deleteMessage(message);
					if (gameState == 0)
						RoboopsUtil.sendMessage(channel, Emote.NO + "You cannot buy items until the game has started.");
					else {
						Item item = Item.fromName(String.join(" ", args));
						if (item == null)
							RoboopsUtil.sendMessage(channel, Emote.NO + "Invalid item.");
						else
							curMember.act(new BuyAction(item));
					}
				} else if (curMember.equals(author) && alias.equalsIgnoreCase("use")) {
					RoboopsUtil.deleteMessage(message);
					if (gameState == 0)
						RoboopsUtil.sendMessage(channel, Emote.NO + "You cannot use items until the game has started.");
					else {
						Item item = Item.fromName(String.join(" ", args));
						if (item == null)
							RoboopsUtil.sendMessage(channel, Emote.NO + "Invalid item.");
						else
							curMember.act(new UseAction(item));
					}
				} else if (curMember.equals(author) && alias.equalsIgnoreCase("bash")) {
					RoboopsUtil.deleteMessage(message);
					if (gameState == 0)
						RoboopsUtil.sendMessage(channel, Emote.NO + "You cannot use **Bash** until the game has started.");
					else {
						Member target = getAlive().stream().filter(m -> !m.equals(curMember)).findAny().orElse(null);
						if (target == null)
							RoboopsUtil.sendMessage(channel, Emote.NO + "There is no one to use **Bash** on.");
						else
							curMember.act(new BashAction(target));
					}
				} else if (curMember.equals(author) && alias.equalsIgnoreCase("rage")) {
					RoboopsUtil.deleteMessage(message);
					if (gameState == 0)
						RoboopsUtil.sendMessage(channel, Emote.NO + "You cannot use **Rage** until the game has started.");
					else
						curMember.act(new RageAction());
				} else if (getAlive().contains(author) && alias.equalsIgnoreCase("forfeit")) {
					RoboopsUtil.deleteMessage(message);
					RoboopsUtil.sendMessage(channel, getAlive().get(getAlive().indexOf(author)).lose());
				}
			}
		}
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
			RoboopsUtil.sendMessage(channel, actor.damage(target));
			actor.stats.gold += RoboopsUtil.randInt(15, 25);
			return true;
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
			List<Item> build = new ArrayList<>(actor.items);
			for (Item i : item.getBuild())
				if (build.contains(i)) {
					cost -= i.getCost();
					build.remove(i);
				}

			if (actor.stats.gold < cost)
				RoboopsUtil.sendMessage(channel, Emote.NO + "You need **" + (cost - actor.stats.gold)
						+ "** more gold. You have **" + actor.stats.gold + "**.");
			else if (build.size() >= 6)
				RoboopsUtil.sendMessage(channel, Emote.NO + "You do not have enough inventory space.");
			else {
				String output = "";
				actor.stats.gold -= cost;
				actor.items.add(item);
				actor.items.removeAll(Arrays.asList(item.getBuild()));
				actor.updateStats();

				if (item.getStats().maxHp > 0 && !actor.shields.contains(item)) {
					output += actor.shield(item.getStats().maxHp);
					actor.shields.add(item);
				}

				RoboopsUtil.sendMessage(channel, Emote.BUY + "**" + actor.getName() + "** purchased a(n) **"
						+ item.getName() + "** for **" + cost + "** gold.\n" + output);
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
			if (!actor.items.contains(item))
				RoboopsUtil.sendMessage(channel, Emote.NO + "You don't have a(n) **" + item.getName() + "**.");
			else if (!item.canUse())
				RoboopsUtil.sendMessage(channel, Emote.NO + "That item can't be used.");
			else {
				RoboopsUtil.sendMessage(channel, Emote.USE + "**" + actor.getName() + "** used a(n) **"
						+ item.getName() + "**.\n" + item.onUse(actor));
				if (item.removeOnUse()) actor.items.remove(item);
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
			if (!(actor.unit instanceof WarriorUnit))
				RoboopsUtil.sendMessage(channel, Emote.NO + "You are not playing **Warrior**.");
			else {
				WarriorUnit wu = (WarriorUnit) actor.unit;
				if (wu.getBash())
					RoboopsUtil.sendMessage(channel, Emote.NO + "You can only use **Bash** once per turn.");
				else {
					wu.setBash(true);

					int damage = Math.round(actor.stats.damage * 0.4f);
					String output = "";
					String bonus = "";

					if (target.stats.shield > 0)
						output += Emote.SHIELD + "**" + actor.getName()
								+ "** destroyed **" + target.getName() + "'s Shield**!\n";

					bonus += actor.damage(target, damage);

					output += Emote.SHIELD + "**" + actor.getName() + "** bashed **"
							+ target.getName() + "** by **" + damage + "**! [**" + target.stats.hp
							+ " / " + target.stats.maxHp + "**]\n";

					RoboopsUtil.sendMessage(channel, output + bonus);
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
			if (!(actor.unit instanceof BerserkerUnit))
				RoboopsUtil.sendMessage(channel, Emote.NO + "You are not playing **Berserker**.");
			else {
				BerserkerUnit bu = (BerserkerUnit) actor.unit;
				if (bu.getRage() < 2)
					RoboopsUtil.sendMessage(channel, Emote.NO + "You need at least **2** stacks to use **Rage**.");
				else {
					int energy = (int) Math.floor(bu.getRage() / 2) * 25 + (bu.getRage() == 6 ? 50 : 0);
					actor.stats.energy += energy;
					bu.setRage(0);
					RoboopsUtil.sendMessage(channel, Emote.RAGE + "**" + actor.getName()
							+ "** has gained **" + energy + "** energy this turn!");
					return true;
				}
			}
			return false;
		}

		@Override
		public int getEnergy() {
			return 0;
		}
	}

	public class Member {
		private Player player;
		private Unit unit;
		private boolean alive = true;
		private int defend = 0;

		private ChanceBag critBag = new ChanceBag();

		private Stats stats = new Stats();
		private Stats perTurn = new Stats();
		private List<Item> items = new ArrayList<>();
		private List<Effect> effects = new ArrayList<>();
		private List<Item> shields = new ArrayList<>();

		public Member(Player player) {
			this.player = player;
		}

		public Player getPlayer() {
			return player;
		}

		public IUser getUser() {
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

		public Stats getStats() {
			return stats;
		}

		public Stats getPerTurn() {
			return perTurn;
		}

		public List<Item> getItems() {
			return items;
		}

		public List<Effect> getEffects() {
			return effects;
		}

		public Effect getEffect(Class clazz) {
			return effects.stream().filter(e -> e.getClass().equals(clazz)).findAny().orElse(null);
		}

		public boolean hasEffect(Class clazz) {
			return getEffect(clazz) != null;
		}

		public Unit getUnit() {
			return unit;
		}

		public void setUnit(Unit unit) {
			this.unit = unit;
			updateStats();

			stats.hp = stats.maxHp;
			stats.gold = 300 - (perTurn.gold * (1 - getAlive().indexOf(this)));

			if (unit instanceof BerserkerUnit)
				((BerserkerUnit) unit).setRage(getAlive().indexOf(this));
		}

		public void updateStats() {
			stats.maxHp = unit.getStats().maxHp;
			stats.damage = unit.getStats().damage;
			stats.critChance = unit.getStats().critChance;
			stats.lifeSteal = unit.getStats().lifeSteal;
			perTurn.hp = unit.getPerTurn().hp;
			perTurn.gold = unit.getPerTurn().gold;
			perTurn.energy = unit.getPerTurn().energy;

			for (Item i : items) {
				stats.add(i.getStats());
				perTurn.add(i.getPerTurn());
				for (Effect e : i.getEffects()) {
					if (!effects.contains(e)) effects.add(e);
					else if (effects.contains(e)) {
						Effect old = effects.get(effects.indexOf(e));
						if (e.getPower() > old.getPower()) {
							effects.remove(old);
							effects.add(e);
						}
					}
				}
			}

			for (Effect e : effects) {
				stats.add(e.getStats());
				perTurn.add(e.getPerTurn());
			}

			critBag.setChance(stats.critChance);
		}

		public void act(Action action) {
			if (stats.energy < action.getEnergy())
				RoboopsUtil.sendMessage(channel, Emote.NO + "You do not have **" + action.getEnergy() + "** energy.");
			else if (action.execute(this)) {
				stats.energy -= action.getEnergy();
				if (stats.energy <= 0) nextTurn();
				else setTopic(this);
			}
		}

		public String shield(int amount) {
			stats.shield += amount;
			return Emote.HEAL + "**" + getName() + "** shielded by **" + amount + "**! [**" + stats.shield + "**]";
		}

		public String heal(int amount) {
			stats.hp = Math.min(stats.maxHp, stats.hp + amount);
			return Emote.HEAL + "**" + getName() + "** healed by **" + amount + "**! [**"
					+ stats.hp + " / " + stats.maxHp + "**]\n";
		}

		public String damage(Member target) {
			String out = "";
			String bonus = "";

			int damage = stats.damage;
			boolean crit = false;

			if (hasEffect(LoveOfWar.class)) {
				LoveOfWar low = (LoveOfWar) getEffect(LoveOfWar.class);
				damage *= 1 + ((low.attack() - 1) * low.getPower());
			}

			if (unit instanceof WarriorUnit) {
				WarriorUnit wu = (WarriorUnit) unit;
				if (wu.bonus() >= 3) {
					damage *= 1.25;
					wu.setBonus(0);
				}
			}

			if (target.unit instanceof BerserkerUnit)
				((BerserkerUnit) target.unit).rage();

			if (stats.lifeSteal > 0)
				bonus += heal(Math.round(stats.lifeSteal * damage));

			if (critBag.get()) {
				crit = true;
				float critAmt = 1.5f + stats.critDamage;

				if (unit instanceof ThiefUnit) {
					ThiefUnit tu = (ThiefUnit) unit;
					critAmt += tu.getCrit() * 0.1f;
					if (tu.crit() == 1) {
						int steal = Math.round(Math.max(1, Math.min(stats.damage * 0.4f, target.stats.gold)));
						target.stats.gold = Math.max(0, target.stats.gold - steal);
						bonus += Emote.BUY + "**" + getName() + "** stole **" + steal + "** gold.\n";
					}
				}

				damage *= Math.max(1, critAmt);
			}

			if (target.defend == 1) damage *= 0.8f;

			if (target.stats.shield > 0) {
				int shieldDmg = Math.round(Math.max(0, Math.min(damage, target.stats.shield)));
				target.stats.shield -= shieldDmg;

				if (target.stats.shield > 0)
					out += Emote.SHIELD + "**" + getName() + "** damaged **" + target.getName() + "'s Shield** by **"
							+ shieldDmg + "**! " + (crit ? "**CRIT**! " : "") + "[**" + target.stats.shield + "**]\n";
				else {
					damage -= shieldDmg;
					out += Emote.SHIELD + "**" + getName() + "** destroyed **" + target.getName() + "'s Shield**!\n";
				}
			}

			if (target.stats.shield <= 0) {
				bonus += damage(target, damage);
				out += Emote.ATTACK + "**" + getName() + "** damaged **" + target.getName() + "** by **"
						+ damage + "**! " + (crit ? "**CRIT**! " : "")
						+ "[**" + target.stats.hp + " / " + target.stats.maxHp + "**]\n";
			}

			return out + bonus;
		}

		public String damage(Member target, int damage) {
			String output = "";
			target.stats.hp -= damage;
			if (target.stats.hp <= 0) output += target.lose();
			return output;
		}

		public String win() {
			Enigma.endGame(Game.this);
			return Emote.TROPHY + getUser() + ", you have won the battle!\n";
		}

		public String lose() {
			String bonus = "";
			alive = false;

			if (getAlive().size() == 1) {
				gameState = 2;
				bonus += getAlive().get(0).win();
			} else if (curMember.equals(this))
				nextTurn();

			return Emote.SKULL + getUser() + " has been slain and removed from the battle.\n" + bonus;
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && getUser().equals(obj);
		}

		@Override
		public String toString() {
			return player.toString();
		}
	}
}
