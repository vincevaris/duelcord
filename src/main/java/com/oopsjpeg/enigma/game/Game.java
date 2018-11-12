package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.effect.LoveOfWar;
import com.oopsjpeg.enigma.game.effect.util.Effect;
import com.oopsjpeg.enigma.game.item.util.Item;
import com.oopsjpeg.enigma.game.unit.BerserkerUnit;
import com.oopsjpeg.enigma.game.unit.GunslingerUnit;
import com.oopsjpeg.enigma.game.unit.ThiefUnit;
import com.oopsjpeg.enigma.game.unit.WarriorUnit;
import com.oopsjpeg.enigma.game.unit.util.Unit;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.ChanceBag;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.roboops.framework.Bufferer;
import com.oopsjpeg.roboops.framework.RoUtil;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;

import java.time.LocalDateTime;
import java.util.*;
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

		Bufferer.overrideRolePermissions(channel, guild.getEveryoneRole(),
				EnumSet.noneOf(Permissions.class), EnumSet.of(Permissions.READ_MESSAGES));
		for (Player player : players)
			Bufferer.overrideUserPermissions(channel, player.getUser(),
					EnumSet.of(Permissions.READ_MESSAGES), EnumSet.noneOf(Permissions.class));

		this.mode = mode;
		members = players.stream().map(Member::new).collect(Collectors.toList());
		nextTurn();

		Enigma.getClient().getDispatcher().registerListeners(new GameListener());
	}

	public void nextTurn() {
		String extra = "";

		if (curTurn >= getAlive().size()) {
			curTurn = 0;
			if (gameState == 0) gameState = 1;
		}

		if (turnCount >= 1 && gameState == 1 && curMember.stats.get(Stats.ENERGY) > 0) {
			curMember.defend = 1;
			extra += curMember.unit.onDefend(curMember);
		}

		if (gameState == 0) {
			curMember = getAlive().get(curTurn);
			if (curTurn == 0) {
				Bufferer.sendMessage(channel, "Welcome to **" + mode.getName() + "**! ("
						+ getPlayers().stream().map(p -> p.getUser().getName()).collect(Collectors.joining(", ")) + ")\n"
						+ curMember + ", you have first pick!\n"
						+ "Check " + Enigma.getUnitsChannel() + " to view available units.");
			} else {
				Bufferer.sendMessage(channel, curMember + ", you have next pick!");
			}
		} else if (gameState == 1) {
			extra += curMember.unit.onTurnEnd(curMember);
			extra += curMember.effects.stream()
					.map(e -> e.onTurnEnd(curMember))
					.collect(Collectors.joining());

			curMember = getAlive().get(curTurn);

			curMember.stats.add(Stats.HP, curMember.perTurn.get(Stats.HP) * curMember.defend);
			curMember.stats.add(Stats.GOLD, Math.round(curMember.perTurn.get(Stats.GOLD) + (turnCount * 0.5)));
			curMember.stats.put(Stats.ENERGY, curMember.unit.getStats().get(Stats.ENERGY));
			curMember.stats.add(Stats.ENERGY, curMember.perTurn.get(Stats.ENERGY));
			curMember.stats.put(Stats.SHIELD, 0);

			extra += curMember.unit.onTurnStart(curMember);
			extra += curMember.effects.stream()
					.map(e -> e.onTurnStart(curMember))
					.collect(Collectors.joining());
			curMember.defend = 0;

			if (turnCount == 0) {
				Bufferer.sendMessage(channel, extra + curMember + ", you have the first turn!\n"
						+ "Open the channel's description to review your statistics.\n"
						+ "Check " + Enigma.getItemsChannel() + " to view purchasable items.");
			} else {
				Bufferer.sendMessage(channel, extra + curMember + ", it's your turn!\n"
						+ "Open the channel's description to review your statistics.");
			}
			turnCount++;
		}

		setTopic(curMember);
		curTurn++;
	}

	private void setTopic(Member member) {
		if (gameState == 0) {
			Bufferer.changeTopic(channel, member + " is picking their unit.");
		} else {
			Bufferer.changeTopic(channel, member.unit.getName() + " " + member + " (" + turnCount + ") - \n\n"
					+ "Gold: **" + member.stats.getInt(Stats.GOLD) + "**\n"
					+ "Health: **" + member.stats.getInt(Stats.HP) + " / " + member.stats.getInt(Stats.MAX_HP)
					+ "** (+**" + member.perTurn.getInt(Stats.HP) + "**/t)\n"
					+ "Energy: **" + member.stats.getInt(Stats.ENERGY) + "**\n"
					+ (member.unit instanceof WarriorUnit
					? "Strike: **" + ((WarriorUnit) member.unit).getBonus() + " / 3**\n" : "")
					+ (member.unit instanceof BerserkerUnit
					? "Rage: **" + ((BerserkerUnit) member.unit).getRage() + " / 6**\n" : "")
					+ (member.unit instanceof GunslingerUnit
					? "Shot: **" + ((GunslingerUnit) member.unit).getShot() + " / 4**\n" : "")
					+ "Items: **" + member.items + "**\n");
		}
	}

	public IChannel getChannel() {
		return channel;
	}

	public GameMode getMode() {
		return mode;
	}

	public Member getMember(IUser user) {
		return members.stream()
				.filter(m -> m.getUser().equals(user))
				.findAny().orElse(null);
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
			Bufferer.sendMessage(channel, Emote.WARN + curMember + ", you have around **4** minutes " +
					"to make an action, otherwise you will **forfeit due to AFKing**.");
		else if (notifyAfk >= 8)
			Bufferer.sendMessage(channel, curMember.lose());
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

				Bufferer.deleteMessage(message);
				if (alias.equalsIgnoreCase("refresh") || alias.equalsIgnoreCase("refreshtopic")) {
					setTopic(curMember);
				} else if (alias.equalsIgnoreCase("gold")) {
					if (gameState == 0)
						Bufferer.deleteMessage(Bufferer.sendMessage(channel,
								Emote.NO + "You cannot check your gold until the game has started."), 5);
				} else if (curMember.getUser().equals(author) && alias.equalsIgnoreCase("pick")) {
					if (gameState == 1)
						Bufferer.deleteMessage(Bufferer.sendMessage(channel,
								Emote.NO + "You cannot pick a unit after the game has started."), 5);
					else {
						Unit unit = Unit.fromName(String.join(" ", args));
						if (unit == null)
							Bufferer.deleteMessage(Bufferer.sendMessage(channel,
									Emote.NO + "Invalid unit. Please try again."), 5);
						else {
							curMember.setUnit(unit);
							Bufferer.sendMessage(channel, Emote.YES + " **" + author.getName() + "** has selected **"
									+ unit.getName() + "**.");
							nextTurn();
						}
					}
				} else if (curMember.getUser().equals(author) && alias.equalsIgnoreCase("end")) {
					if (gameState == 0)
						Bufferer.deleteMessage(Bufferer.sendMessage(channel,
								Emote.NO + "You cannot end your turn until the game has started."), 5);
					else
						nextTurn();
				} else if (curMember.getUser().equals(author) && alias.equalsIgnoreCase("attack")) {
					if (gameState == 0)
						Bufferer.deleteMessage(Bufferer.sendMessage(channel,
								Emote.NO + "You cannot attack until the game has started."), 5);
					else {
						Member target = getAlive().stream().filter(m -> !m.equals(curMember)).findAny().orElse(null);
						if (target == null)
							Bufferer.deleteMessage(Bufferer.sendMessage(channel,
									Emote.NO + "There is no one to attack."), 5);
						else
							curMember.act(new AttackAction(target));
					}
				} else if (curMember.getUser().equals(author) && alias.equalsIgnoreCase("buy")) {
					if (gameState == 0)
						Bufferer.deleteMessage(Bufferer.sendMessage(channel,
								Emote.NO + "You cannot buy items until the game has started."), 5);
					else {
						Item item = Item.fromName(String.join(" ", args));
						if (item == null)
							Bufferer.deleteMessage(Bufferer.sendMessage(channel,
									Emote.NO + "Invalid item. Please try again."), 5);
						else
							curMember.act(new BuyAction(item));
					}
				} else if (curMember.getUser().equals(author) && alias.equalsIgnoreCase("use")) {
					if (gameState == 0)
						Bufferer.deleteMessage(Bufferer.sendMessage(channel,
								Emote.NO + "You cannot use items until the game has started."), 5);
					else {
						Item item = Item.fromName(String.join(" ", args));
						if (item == null)
							Bufferer.deleteMessage(Bufferer.sendMessage(channel,
									Emote.NO + "Invalid item. Please try again."), 5);
						else
							curMember.act(new UseAction(item));
					}
				} else if (curMember.getUser().equals(author) && alias.equalsIgnoreCase("bash")) {
					if (gameState == 0)
						Bufferer.deleteMessage(Bufferer.sendMessage(channel,
								Emote.NO + "You cannot use **Bash** until the game has started."), 5);
					else {
						Member target = getAlive().stream().filter(m -> !m.equals(curMember)).findAny().orElse(null);
						if (target == null)
							Bufferer.deleteMessage(Bufferer.sendMessage(channel,
									Emote.NO + "There is no one to use **Bash** on."), 5);
						else
							curMember.act(new BashAction(target));
					}
				} else if (curMember.getUser().equals(author) && alias.equalsIgnoreCase("rage")) {
					if (gameState == 0)
						Bufferer.deleteMessage(Bufferer.sendMessage(channel,
								Emote.NO + "You cannot use **Rage** until the game has started."), 5);
					else
						curMember.act(new RageAction());
				} else if (getAlive().contains(getMember(author)) && (alias.equalsIgnoreCase("forfeit")
						|| alias.equalsIgnoreCase("ff"))) {
					Bufferer.sendMessage(channel, getMember(author).lose());
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
			Bufferer.sendMessage(channel, actor.damage(target));
			actor.stats.add(Stats.GOLD, RoUtil.nextInt(15, 25));
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

			if (actor.stats.get(Stats.GOLD) < cost)
				Bufferer.sendMessage(channel, Emote.NO + "You need **" + (cost - actor.stats.getInt(Stats.GOLD))
						+ "** more gold for a(n) **" + item.getName() + "**.");
			else if (build.size() >= 6)
				Bufferer.sendMessage(channel, Emote.NO
						+ "You do not have enough inventory space for a(n) **" + item.getName() + "**..");
			else {
				String output = "";
				actor.stats.sub(Stats.GOLD, cost);
				actor.items.add(item);
				actor.items.removeAll(Arrays.asList(item.getBuild()));
				actor.updateStats();

				if (item.getStats().get(Stats.MAX_HP) > 0 && !actor.shields.contains(item)) {
					output += actor.shield(item.getStats().get(Stats.MAX_HP));
					actor.shields.add(item);
				}

				Bufferer.sendMessage(channel, Emote.BUY + "**" + actor.getName() + "** purchased a(n) **"
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
				Bufferer.sendMessage(channel, Emote.NO + "You don't have a(n) **" + item.getName() + "**.");
			else if (!item.canUse())
				Bufferer.sendMessage(channel, Emote.NO + "**" + item.getName() + "** can't be used.");
			else {
				Bufferer.sendMessage(channel, Emote.USE + "**" + actor.getName() + "** used a(n) **"
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
				Bufferer.sendMessage(channel, Emote.NO + "You are not playing **Warrior**.");
			else {
				WarriorUnit wu = (WarriorUnit) actor.unit;
				if (wu.getBash())
					Bufferer.sendMessage(channel, Emote.NO + "You can only use **Bash** once per turn.");
				else {
					wu.setBash(true);

					float damage = Math.round(actor.stats.get(Stats.DAMAGE) * 0.4f);
					String output = "";
					String bonus = "";

					if (target.stats.get(Stats.SHIELD) > 0)
						output += Emote.SHIELD + "**" + actor.getName()
								+ "** destroyed **" + target.getName() + "'s Shield**!\n";

					bonus += actor.damage(target, damage);

					output += Emote.SHIELD + "**" + actor.getName() + "** bashed **"
							+ target.getName() + "** by **" + damage + "**! [**"
							+ target.stats.getInt(Stats.HP) + " / " + target.stats.getInt(Stats.MAX_HP) + "**]\n";

					Bufferer.sendMessage(channel, output + bonus);
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
				Bufferer.sendMessage(channel, Emote.NO + "You are not playing **Berserker**.");
			else {
				BerserkerUnit bu = (BerserkerUnit) actor.unit;
				if (bu.getRage() < 2)
					Bufferer.sendMessage(channel, Emote.NO + "You need at least **2** stacks to use **Rage**.");
				else {
					int energy = (int) Math.floor((float) bu.getRage() / 2) * 25 + (bu.getRage() == 6 ? 50 : 0);
					actor.stats.add(Stats.ENERGY, energy);
					bu.setRage(0);
					Bufferer.sendMessage(channel, Emote.RAGE + "**" + actor.getName()
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
		private Map<Object, Object> data = new HashMap<>();

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

			stats.put(Stats.HP, stats.get(Stats.MAX_HP));
			stats.put(Stats.GOLD, 300 - (perTurn.get(Stats.GOLD) * (1 - getAlive().indexOf(this))));

			if (unit instanceof BerserkerUnit)
				((BerserkerUnit) unit).setRage(getAlive().indexOf(this));
		}

		public void updateStats() {
			stats.put(Stats.MAX_HP, unit.getStats().get(Stats.MAX_HP));
			stats.put(Stats.DAMAGE, unit.getStats().get(Stats.DAMAGE));
			stats.put(Stats.ACCURACY, unit.getStats().get(Stats.ACCURACY));
			stats.put(Stats.CRIT_CHANCE, unit.getStats().get(Stats.CRIT_CHANCE));
			stats.put(Stats.CRIT_DAMAGE, unit.getStats().get(Stats.CRIT_DAMAGE));
			stats.put(Stats.LIFE_STEAL, unit.getStats().get(Stats.LIFE_STEAL));
			perTurn.put(Stats.HP, unit.getPerTurn().get(Stats.HP));
			perTurn.put(Stats.GOLD, unit.getPerTurn().get(Stats.GOLD));
			perTurn.put(Stats.ENERGY, unit.getPerTurn().get(Stats.ENERGY));

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

			if (unit instanceof GunslingerUnit)
				stats.mul(Stats.DAMAGE, 1 + (stats.get(Stats.CRIT_CHANCE) * 0.75f));

			critBag.setChance(stats.get(Stats.CRIT_CHANCE));
		}

		public void act(Action action) {
			if (stats.get(Stats.ENERGY) < action.getEnergy())
				Bufferer.sendMessage(channel, Emote.NO + "You do not have **" + action.getEnergy() + "** energy.");
			else if (action.execute(this)) {
				stats.sub(Stats.ENERGY, action.getEnergy());
				if (stats.get(Stats.ENERGY) <= 0) nextTurn();
				else setTopic(this);
			}
		}

		public String shield(float amount) {
			stats.add(Stats.SHIELD, amount);
			return Emote.HEAL + "**" + getName() + "** shielded by **" + Math.round(amount)
					+ "**! [**" + stats.getInt(Stats.SHIELD) + "**]\n";
		}

		public String heal(float amount) {
			stats.add(Stats.HP, amount);
			return Emote.HEAL + "**" + getName() + "** healed by **" + Math.round(amount) + "**! [**"
					+ stats.getInt(Stats.HP) + " / " + stats.getInt(Stats.MAX_HP) + "**]\n";
		}

		public String damage(Member target) {
			String out = "";
			String bonus = "";

			float damage = stats.get(Stats.DAMAGE);
			boolean crit = false;

			if (hasEffect(LoveOfWar.class)) {
				LoveOfWar low = (LoveOfWar) getEffect(LoveOfWar.class);
				damage *= 1 + ((low.attack() - 1) * low.getPower());
			}

			if (unit instanceof GunslingerUnit && ((GunslingerUnit) unit).shot() >= 4) {
				((GunslingerUnit) unit).setShot(0);
				crit = true;
			} else if (unit.isRanged() && stats.get(Stats.ACCURACY) < 1
					&& RoUtil.RANDOM.nextFloat() > stats.get(Stats.ACCURACY)) {
				damage *= 0.4f;
				bonus += Emote.GUN + "**" + getName() + "** missed the target!\n";
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

			if (stats.get(Stats.LIFE_STEAL) > 0)
				bonus += heal(Math.round(stats.get(Stats.LIFE_STEAL) * damage));

			if ((!(unit instanceof GunslingerUnit) && critBag.get()) || crit) {
				crit = true;
				float critAmt = 1.5f + stats.get(Stats.CRIT_DAMAGE);

				if (unit instanceof ThiefUnit) {
					ThiefUnit tu = (ThiefUnit) unit;
					critAmt += tu.getCrit() * 0.1f;
					if (tu.crit() == 1) {
						int steal = Math.round(Math.max(1, Math.min(stats.get(Stats.DAMAGE) * 0.4f,
								target.stats.get(Stats.GOLD))));
						stats.add(Stats.GOLD, steal);
						target.stats.sub(Stats.GOLD, steal);
						bonus += Emote.BUY + "**" + getName() + "** stole **" + steal + "** gold!\n";
					}
				}

				damage *= Math.max(1, critAmt);
			}

			if (target.defend == 1) damage *= 0.8f;

			if (target.stats.get(Stats.SHIELD) > 0) {
				float shieldDmg = Math.max(0, Math.min(damage, target.stats.get(Stats.SHIELD)));
				target.stats.sub(Stats.SHIELD, shieldDmg);

				if (target.stats.get(Stats.SHIELD) > 0)
					out += Emote.SHIELD + "**" + getName() + "** damaged **" + target.getName() + "'s Shield** by **"
							+ Math.round(shieldDmg) + "**! " + (crit ? "**CRIT**! " : "")
							+ "[**" + target.stats.getInt(Stats.SHIELD) + "**]\n";
				else {
					damage -= shieldDmg;
					out += Emote.SHIELD + "**" + getName() + "** destroyed **" + target.getName() + "'s Shield**!\n";
				}
			}

			if (target.stats.get(Stats.SHIELD) <= 0) {
				bonus += damage(target, damage);
				out += Emote.ATTACK + "**" + getName() + "** damaged **" + target.getName() + "** by **"
						+ Math.round(damage) + "**! " + (crit ? "**CRIT**! " : "")
						+ "[**" + target.stats.getInt(Stats.HP) + " / " + target.stats.getInt(Stats.MAX_HP) + "**]\n";
			}

			return out + bonus;
		}

		public String damage(Member target, float damage) {
			String output = "";
			target.stats.sub(Stats.HP, damage);
			if (target.stats.get(Stats.HP) <= 0) output += target.lose();
			return output;
		}

		public String win() {
			Enigma.endGame(Game.this);
			return Emote.TROPHY + getUser() + ", you have won the game!\n";
		}

		public String lose() {
			String bonus = "";
			alive = false;

			if (getAlive().size() == 1) {
				gameState = 2;
				bonus += getAlive().get(0).win();
			} else if (curMember.equals(this))
				nextTurn();

			return Emote.SKULL + getUser() + " has been slain and removed from the game!\n" + bonus;
		}

		@Override
		public String toString() {
			return getUser().toString();
		}
	}
}
