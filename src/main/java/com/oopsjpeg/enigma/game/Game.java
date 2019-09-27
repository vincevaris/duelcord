package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.commands.game.*;
import com.oopsjpeg.enigma.game.buff.Bleed;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.game.buff.Wound;
import com.oopsjpeg.enigma.game.effect.LoveOfWar;
import com.oopsjpeg.enigma.game.effect.Wounder;
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
		commands.add(new BashCommand());
		commands.add(new BuyCommand());
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
			output.add(curMember.unit.onTurnEnd(curMember));
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

			curMember.stats.add(Stats.HP, curMember.perTurn.get(Stats.HP) * curMember.defend);
			curMember.stats.add(Stats.GOLD, Math.round(curMember.perTurn.get(Stats.GOLD) + (turnCount * 0.5)));
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

			output.add(curMember.unit.onTurnStart(curMember));
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
					+ (member.unit instanceof Gunslinger
					? "Shot: **" + ((Gunslinger) member.unit).getShot() + " / 4**\n" : "")
					+ (member.unit instanceof Duelist
					? "Attack: **" + ((Duelist) member.unit).getAttack() + " / 4**\n" : "")
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
				channel.sendMessage(actor.damage(target)).complete();
				actor.stats.add(Stats.GOLD, Util.nextInt(15, 25));
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

				output.add(Emote.BUY + "**" + actor.getName() + "** purchased a(n) **"
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

					float damage = actor.stats.get(Stats.DAMAGE) * 0.5f * actor.stats.get(Stats.ABILITY_POWER);
					List<String> output = new ArrayList<>();

					if (target.stats.get(Stats.SHIELD) > 0) {
						target.stats.put(Stats.SHIELD, 0);
						output.add(Emote.SHIELD + "**" + actor.getName()
								+ "** destroyed **" + target.getName() + "'s Shield**!");
					}

					output.add(actor.damage(target, damage));

					output.add(0, Emote.KNIFE + "**" + actor.getName() + "** bashed **"
							+ target.getName() + "** by **" + Math.round(damage) + "**! [**"
							+ target.stats.getInt(Stats.HP) + " / " + target.stats.getInt(Stats.MAX_HP) + "**]");

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

	public class RageAction extends Action {
		@Override
		public boolean act(Member actor) {
			if (!(actor.unit instanceof Berserker))
				Util.sendError(channel, "You are not playing **Berserker**.");
			else if (actor.hasData(Silence.class))
				Util.sendError(channel, "You cannot **Rage** while silenced.");
			else {
				Berserker berserk = (Berserker) actor.unit;
				berserk.setBonus(0.04f * berserk.getRage() * actor.stats.get(Stats.ABILITY_POWER));

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
					List<String> output = new ArrayList<>();
					au.setSlashed(true);

					float damage = actor.stats.get(Stats.DAMAGE) * Assassin.SLASH_DAMAGE * actor.stats.get(Stats.ABILITY_POWER);
					if (au.slashCount() >= 4) {
						damage += au.getPotency();
						output.add(target.buff(new Silence(actor, Assassin.SILENCE_TURNS)));
						au.setSlashCount(0);
						au.setPotencyTurn(0);
						au.setPotency(0);
					}

					output.add(actor.damage(target, damage));

					output.add(0, Emote.KNIFE + "**" + actor.getName() + "** slashed **"
							+ target.getName() + "** by **" + Math.round(damage) + "**! [**"
							+ target.stats.getInt(Stats.HP) + " / " + target.stats.getInt(Stats.MAX_HP) + "**]");

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
			stats.put(Stats.ACCURACY, unit.getStats().get(Stats.ACCURACY));
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

			if (unit instanceof Gunslinger)
				stats.mul(Stats.DAMAGE, 1 + (stats.get(Stats.CRIT_CHANCE) * 0.75f));

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
			if (hasData(buff.getClass())) {
				Buff oldBuff = (Buff) getData(buff.getClass());
				if (buff.getPower() > oldBuff.getPower()) {
					data.remove(oldBuff);
					data.add(buff);
					return Emote.DEBUFF + "**" + getName() + "** applied **" + buff.getName() + "** for **" + buff.getTurns() + "** turn(s)!";
				}
				return "";
			}

			data.add(buff);
			return Emote.DEBUFF + "**" + getName() + "** applied **" + buff.getName() + "** for **" + buff.getTurns() + "** turn(s)!";
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
			amount *= 1 - ((Wound) getData(Wound.class)).getPower();
			stats.add(Stats.HP, amount);
			return Emote.HEAL + "**" + getName() + "** healed by **" + Math.round(amount) + "**! [**"
					+ stats.getInt(Stats.HP) + " / " + stats.getInt(Stats.MAX_HP) + "**] (" + (source.isEmpty() ? "" : source) + ")";
		}

		public String damage(Member target) {
			List<String> output = new ArrayList<>();

			float damage = stats.get(Stats.DAMAGE);
			float bonus = 0;
			boolean crit = false;
			boolean miss = false;

			// Missed attack damage reduction
			if (unit.isRanged() && stats.get(Stats.ACCURACY) < 1
					&& Util.RANDOM.nextFloat() <= stats.get(Stats.ACCURACY)) {
				damage *= 0.4f;
				miss = true;
			}

			// Love of War damage multiplier
			if (hasData(LoveOfWar.class)) {
				LoveOfWar low = (LoveOfWar) getData(LoveOfWar.class);
				damage *= 1 + ((low.attack() - 1) * low.getPower());
			}

			// Wounder effect
			if (hasData(Wounder.class)) {
				Wounder wor = (Wounder) getData(Wounder.class);
				output.add(target.buff(new Wound(this, 1, wor.getPower())));
			}

			// Warrior bonus damage
			if (unit instanceof Warrior && ((Warrior) unit).bonus() >= 3) {
				float bonusDmg = damage * 0.3f * stats.get(Stats.ABILITY_POWER);
				damage += bonusDmg;
				bonus += bonusDmg;
				((Warrior) unit).setBonus(0);
			}

			// Berserker attacker checks
			if (unit instanceof Berserker) {
				if (((Berserker) unit).getBonus() > 0)
					// Berserker bonus damage
					damage *= 1 + ((Berserker) unit).getBonus();
				else
					// Berserker rage stack from attacking
					((Berserker) unit).rage();
			}

			// Berserker victim rage stack
			if (target.unit instanceof Berserker && ((Berserker) target.unit).getBonus() <= 0)
				((Berserker) target.unit).rage();

			// Duelist stack
			if (unit instanceof Duelist && ((Duelist) unit).attack() >= 4) {
				((Duelist) unit).setAttack(0);
				float bonusDmg = target.stats.getInt(Stats.MAX_HP) * 0.04f * stats.get(Stats.ABILITY_POWER);
				float bleedDmg = stats.get(Stats.DAMAGE) * 0.4f * stats.get(Stats.ABILITY_POWER);
				output.add(target.buff(new Bleed(this, 2, bleedDmg)));
				damage += bonusDmg;
				bonus += bonusDmg;
			}

			// Assassin potency stacking
			if (unit instanceof Assassin && ((Assassin) unit).getPotencyTurn() < Assassin.POTENCY_TURNS) {
				float potency = damage * Math.min(Assassin.POTENCY_STACK_MAX, Assassin.POTENCY_STACK_MIN + (turnCount * 0.005f));
				((Assassin) unit).addPotency(potency);
				((Assassin) unit).addPotencyNow(potency);
			}

			// Crit checks
			if (!miss) {
				if (unit instanceof Gunslinger && ((Gunslinger) unit).shot() >= 4) {
					// Gunslinger passive crit
					crit = true;
					((Gunslinger) unit).setShot(0);
				} else if (critBag.get()) {
					// Pseudo RNG crit bag
					crit = true;
				}
			}

			// Critical strike bonus damage
			if (crit) {
				float critMul = 1.5f + stats.get(Stats.CRIT_DAMAGE);

				// Thief bonus crit damage + gold steal
				if (unit instanceof Thief) {
					critMul += ((Thief) unit).getCrit() * 0.2f;
					if (((Thief) unit).crit() == 1) {
						int steal = (int) Math.min(stats.get(Stats.DAMAGE) * 0.4f * stats.get(Stats.ABILITY_POWER), target.stats.getInt(Stats.GOLD));
						stats.add(Stats.GOLD, steal);
						target.stats.sub(Stats.GOLD, steal);
						output.add(Emote.BUY + "**" + getName() + "** stole **" + steal + "** gold!");
					}
				}

				damage *= Math.max(1, critMul);
			}

			// Life steal healing
			if (stats.get(Stats.LIFE_STEAL) > 0)
				output.add(heal(Math.round(stats.get(Stats.LIFE_STEAL) * damage)));

			// Shield damaging
			if (target.stats.get(Stats.SHIELD) > 0) {
				float shieldDmg = Util.limit(target.stats.get(Stats.SHIELD), 0, damage);
				target.stats.sub(Stats.SHIELD, shieldDmg);

				if (target.stats.get(Stats.SHIELD) > 0)
					output.add(0, Emote.SHIELD + "**" + getName() + "** damaged **" + target.getName() + "'s Shield** by **"
							+ Math.round(shieldDmg - bonus) + bonusText(bonus) + "**!" + damageText(crit, miss)
							+ " [**" + target.stats.getInt(Stats.SHIELD) + "**]");
				else {
					damage -= shieldDmg;
					output.add(0, Emote.SHIELD + "**" + getName() + "** destroyed **" + target.getName() + "'s Shield**!");
				}
			}

			// Direct damaging
			if (target.stats.get(Stats.SHIELD) <= 0) {
				output.add(damage(target, damage));
				output.add(0, Emote.ATTACK + "**" + getName() + "** damaged **" + target.getName() + "** by **"
						+ Math.round(damage - bonus) + bonusText(bonus) + "**!" + damageText(crit, miss)
						+ " [**" + target.stats.getInt(Stats.HP) + " / " + target.stats.getInt(Stats.MAX_HP) + "**]");
			}

			return String.join("\n", output);
		}

		public String damage(Member target, float damage) {
			// Defensive stance damage reduction
			if (target.defend == 1) damage *= 0.8f;

			target.stats.sub(Stats.HP, damage);
			if (target.stats.get(Stats.HP) <= 0)
				return target.lose();
			return "";
		}

		public String damageText(boolean crit, boolean miss) {
			return (crit ? " **CRIT**!" : "") + (miss ? " **MISS**!" : "");
		}

		public String bonusText(float bonus) {
			return (bonus > 0 ? " (+" + Math.round(bonus) + ")" : "");
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
