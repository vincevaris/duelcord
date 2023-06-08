package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.BleedingDebuff;
import com.oopsjpeg.enigma.game.buff.CrippledDebuff;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.util.ArrayList;
import java.util.List;

import static com.oopsjpeg.enigma.game.Stats.*;
import static com.oopsjpeg.enigma.game.unit.UnitConstants.*;
import static com.oopsjpeg.enigma.util.Util.percent;
import static com.oopsjpeg.enigma.util.Util.percentRaw;

public enum Unit implements GameObject
{
    ASSASSIN("Assassin", Color.SEA_GREEN, new Stats()
            .put(MAX_ENERGY, 100)
            .put(MAX_HEALTH, 930)
            .put(ATTACK_POWER, 14)
            .put(HEALTH_PER_TURN, 15))
            {
                private static final String VAR_SLASH_COOLDOWN = "slash_cooldown";

                private static final int PASSIVE_DAMAGE_BASE = 10;
                private static final float PASSIVE_DAMAGE_AP_RATIO = .15f;
                private static final float PASSIVE_DAMAGE_SP_RATIO = .6f;
                private static final int PASSIVE_ENERGY_RESTORE = 25;

                private static final int SLASH_COST = 25;
                private static final int SLASH_COOLDOWN = 2;
                private static final int SLASH_DAMAGE_BASE = 15;
                private static final float SLASH_DAMAGE_AP_RATIO = .20f;
                private static final float SLASH_DAMAGE_SP_RATIO = .55f;
                private static final float SLASH_BLEED_CHANCE = .2f;
                private static final int SLASH_BLEED_TURNS = 2;
                private static final float SLASH_BLEED_DAMAGE_RATIO = .2f;

                private static final int MARK_COST = 25;
                private static final int MARK_COOLDOWN = 4;
                private static final float MARK_CRIPPLE = .35f;

                private static final int EXECUTE_COST = 75;
                private static final int EXECUTE_COOLDOWN = 6;
                private static final int EXECUTE_DAMAGE_BASE = 40;
                private static final float EXECUTE_DAMAGE_MISSING_HP = .04f;
                private static final float EXECUTE_DAMAGE_PER_DEBUFF = .065f;

                private static final int CLOAK_COST = 50;
                private static final int CLOAK_COOLDOWN = 4;
                private static final float CLOAK_DODGE = .8f;

                private Cooldown getSlashCooldown(GameMemberVars vars)
                {
                    if (!vars.has(this, VAR_SLASH_COOLDOWN))
                        setSlashCooldown(vars, new Cooldown(SLASH_COOLDOWN));
                    return vars.get(this, VAR_SLASH_COOLDOWN, Cooldown.class);
                }

                private void setSlashCooldown(GameMemberVars vars, Cooldown slashCooldown)
                {
                    vars.put(this, VAR_SLASH_COOLDOWN, slashCooldown);
                }

                @Override
                public String getDescription()
                {
                    return "After using a damaging Skill, your next Attack deals __" + PASSIVE_DAMAGE_BASE + "__ + __" +
                            percent(PASSIVE_DAMAGE_AP_RATIO) + " AP__ + __" + percent(PASSIVE_DAMAGE_SP_RATIO) +
                            " SP__ bonus damage, restores **" + PASSIVE_ENERGY_RESTORE + "** Energy, and resets **Slash** cooldown.";
                }

                @Override
                public String onTurnStart(GameMember member)
                {
                    Game game = member.getGame();
                    GameMember enemy = game.getRandomTarget(member);

                    if (enemy.hasBuff(MarkedDebuff.class))
                    {
                        enemy.removeBuffs(MarkedDebuff.class);
                        enemy.addBuff(new CrippledDebuff(member, 1, MARK_CRIPPLE), Emote.CRIPPLE);
                        return Emote.CRIPPLE + "**" + enemy.getUsername() + "** was marked by the assassin, suffering **Cripple** (" + percent(MARK_CRIPPLE) + ").";
                    }

                    return null;
                }

                @Override
                public DamageEvent skillOut(DamageEvent event)
                {
                    if (!event.cancelled && !event.actor.hasBuff(TracingBuff.class))
                        event.actor.addBuff(new TracingBuff(event.actor), Emote.TRACE);
                    return event;
                }

                @Override
                public Skill[] getSkills()
                {
                    return new Skill[]{new SlashSkill(), new MarkSkill(), new ExecuteSkill(), new CloakSkill()};
                }

                class TracingBuff extends Buff
                {
                    public TracingBuff(GameMember source)
                    {
                        super("Tracing", false, source, 1, 0);
                    }

                    @Override
                    public String getStatus(GameMember member)
                    {
                        return "Tracing: " + getTotalDamage(member.getStats()) + " bonus on Attack, restore " + PASSIVE_ENERGY_RESTORE + " Energy, reset Slash";
                    }

                    public int getTotalDamage(Stats stats)
                    {
                        return Math.round(PASSIVE_DAMAGE_BASE +
                                (stats.get(ATTACK_POWER) * PASSIVE_DAMAGE_AP_RATIO) +
                                (stats.get(SKILL_POWER) * PASSIVE_DAMAGE_SP_RATIO));
                    }

                    @Override
                    public DamageEvent attackOut(DamageEvent event)
                    {
                        GameMemberVars vars = event.actor.getVars();
                        Cooldown slashCooldown = getSlashCooldown(vars);

                        if (!event.cancelled)
                        {
                            Stats stats = event.actor.getStats();
                            event.bonus += PASSIVE_DAMAGE_BASE;
                            event.bonus += stats.get(ATTACK_POWER) * PASSIVE_DAMAGE_AP_RATIO;
                            event.bonus += stats.get(SKILL_POWER) * PASSIVE_DAMAGE_SP_RATIO;
                            event.actor.giveEnergy(PASSIVE_ENERGY_RESTORE);
                            slashCooldown.reset();
                        }

                        setSlashCooldown(vars, slashCooldown);
                        remove(true);

                        return event;
                    }
                }

                class SlashSkill extends Skill
                {
                    public SlashSkill()
                    {
                        super(ASSASSIN, SLASH_COOLDOWN, SLASH_COST);
                    }

                    @Override
                    public String getName()
                    {
                        return "Slash";
                    }

                    @Override
                    public String getDescription()
                    {
                        return "Deal __" + SLASH_DAMAGE_BASE + "__ + __" + percent(SLASH_DAMAGE_AP_RATIO) +
                                " AP__ + __" + percent(SLASH_DAMAGE_SP_RATIO) + " SP__.\n" +
                                "Has a __" + percent(SLASH_BLEED_CHANCE) + "__ chance to Bleed for __" +
                                percent(SLASH_BLEED_DAMAGE_RATIO) + "__ of damage dealt over **" + SLASH_BLEED_TURNS +
                                "** turns.";
                    }

                    @Override
                    public GameAction act(Game game, GameMember actor)
                    {
                        return new SlashAction(game.getRandomTarget(actor));
                    }
                }

                class SlashAction implements GameAction
                {
                    private final GameMember target;

                    public SlashAction(GameMember target)
                    {
                        this.target = target;
                    }

                    @Override
                    public String act(GameMember actor)
                    {
                        List<String> output = new ArrayList<>();
                        output.add(Emote.SKILL + "**" + actor.getUsername() + "** used **Slash**!");

                        DamageEvent event = new DamageEvent(actor, target);
                        Stats stats = actor.getStats();
                        event.damage += SLASH_DAMAGE_BASE;
                        event.damage += stats.get(ATTACK_POWER) * SLASH_DAMAGE_AP_RATIO;
                        event.damage += stats.get(SKILL_POWER) * SLASH_DAMAGE_SP_RATIO;
                        event = actor.skill(event);

                        output.add(actor.damage(event, Emote.KNIFE, "Slash"));

                        if (!event.cancelled)
                        {
                            float rand = Util.RANDOM.nextFloat();
                            if (rand <= SLASH_BLEED_CHANCE)
                            {
                                float bleedDamage = event.damage * SLASH_BLEED_DAMAGE_RATIO;
                                output.add(target.addBuff(new BleedingDebuff(actor, SLASH_BLEED_TURNS, bleedDamage), Emote.BLEED));
                            }
                        }

                        return Util.joinNonEmpty("\n", output);
                    }

                    @Override
                    public int getEnergy()
                    {
                        return SLASH_COST;
                    }
                }

                class MarkSkill extends Skill
                {
                    public MarkSkill()
                    {
                        super(ASSASSIN, MARK_COOLDOWN, MARK_COST);
                    }

                    @Override
                    public String getName()
                    {
                        return "Mark";
                    }

                    @Override
                    public String getDescription()
                    {
                        return "Mark the enemy. If they're still marked on your next turn, consume the mark to Cripple them by __" + percent(MARK_CRIPPLE) + "__.";
                    }

                    @Override
                    public GameAction act(Game game, GameMember actor)
                    {
                        return new MarkAction(game.getRandomTarget(actor));
                    }
                }

                class MarkAction implements GameAction
                {
                    private final GameMember target;

                    public MarkAction(GameMember target)
                    {
                        this.target = target;
                    }

                    @Override
                    public String act(GameMember actor)
                    {
                        target.addBuff(new MarkedDebuff(actor), ":bangbang: ");
                        return ":bangbang: **" + actor.getUsername() + "** used **Mark** on **" + target.getUsername() + "**.";
                    }

                    @Override
                    public int getEnergy()
                    {
                        return MARK_COST;
                    }
                }

                class MarkedDebuff extends Buff
                {
                    public MarkedDebuff(GameMember source)
                    {
                        super("Marked", true, source, 3, 0);
                    }

                    @Override
                    public String getStatus(GameMember member)
                    {
                        return "Marked: Will be Crippled on " + getSource().getUsername() + "'s turn";
                    }

                    @Override
                    public String onTurnStart(GameMember member)
                    {
                        return ":bangbang: **" + member.getUsername() + "** is marked for assassination by **" + getSource().getUsername() + "**.";
                    }
                }

                class ExecuteSkill extends Skill
                {
                    public ExecuteSkill()
                    {
                        super(ASSASSIN, EXECUTE_COOLDOWN, EXECUTE_COST);
                    }

                    @Override
                    public String getName()
                    {
                        return "Execute";
                    }

                    @Override
                    public String getDescription()
                    {
                        return "Deal __" + EXECUTE_DAMAGE_BASE + "__ + __" + percent(EXECUTE_DAMAGE_MISSING_HP) +
                                "__ of enemy missing health, increased by __" + percentRaw(EXECUTE_DAMAGE_PER_DEBUFF) + "__ per debuff they have.";
                    }

                    @Override
                    public GameAction act(Game game, GameMember actor)
                    {
                        return new ExecuteAction(game.getRandomTarget(actor));
                    }
                }

                class ExecuteAction implements GameAction
                {
                    private final GameMember target;

                    public ExecuteAction(GameMember target)
                    {
                        this.target = target;
                    }

                    @Override
                    public String act(GameMember actor)
                    {
                        DamageEvent event = new DamageEvent(actor, target);

                        event.damage += EXECUTE_DAMAGE_BASE;
                        event.damage += EXECUTE_DAMAGE_MISSING_HP * target.getMissingHealth();

                        target.getBuffs().stream()
                                .filter(Buff::isDebuff)
                                .forEach(debuff -> event.bonus += EXECUTE_DAMAGE_PER_DEBUFF * target.getMissingHealth());

                        actor.skill(event);

                        return Emote.SKILL + "**" + actor.getUsername() + "** used **Execute**!\n" + actor.damage(event, Emote.KNIFE, "Execute");
                    }

                    @Override
                    public int getEnergy()
                    {
                        return EXECUTE_COST;
                    }
                }

                class CloakSkill extends Skill
                {
                    public CloakSkill()
                    {
                        super(ASSASSIN, CLOAK_COOLDOWN, CLOAK_COST);
                    }

                    @Override
                    public String getName()
                    {
                        return "Cloak";
                    }

                    @Override
                    public String getDescription()
                    {
                        return "End the turn and gain __" + percent(CLOAK_DODGE) + " Dodge__ until your next turn.\n" +
                                "Enemy damaging Skills end this effect.";
                    }

                    @Override
                    public GameAction act(Game game, GameMember actor)
                    {
                        return new CloakAction();
                    }
                }

                class CloakAction implements GameAction
                {
                    @Override
                    public String act(GameMember actor)
                    {
                        GameMemberVars vars = actor.getVars();
                        Game game = actor.getGame();
                        Stats stats = actor.getStats();;

                        actor.addBuff(new CloakedBuff(actor, CLOAK_DODGE), Emote.NINJA);
                        actor.setEnergy(0);

                        return Emote.NINJA + "**" + actor.getUsername() + "** used **Cloak**, gaining __" + percent(CLOAK_DODGE) + "__ Dodge until damaged by a Skill.";
                    }

                    @Override
                    public int getEnergy()
                    {
                        return CLOAK_COST;
                    }
                }

                class CloakedBuff extends Buff
                {
                    public CloakedBuff(GameMember source, float power)
                    {
                        super("Cloaked", false, source, 2, power);
                    }

                    @Override
                    public DamageEvent skillIn(DamageEvent event)
                    {
                        if (!event.cancelled) remove();
                        return event;
                    }

                    @Override
                    public String getStatus(GameMember member)
                    {
                        return "Cloaked: " + percent(getPower()) + " bonus Dodge until damaged by Skill";
                    }

                    @Override
                    public Stats getStats()
                    {
                        return new Stats()
                                .put(DODGE, getPower());
                    }
                }
            },
    //BERSERKER("Berserker", Color.RED, new Stats()
    //        .put(Stats.MAX_ENERGY, 100)
    //        .put(Stats.MAX_HEALTH, 760)
    //        .put(Stats.DAMAGE, 19)
    //        .put(Stats.HEALTH_PER_TURN, 10)),
    //REAPER("Reaper", Color.of(120, 0, 0), new Stats()
    //        .put(MAX_ENERGY, 125)
    //        .put(Stats.MAX_HEALTH, 720)
    //        .put(Stats.HEALTH_PER_TURN, 7)
    //        .put(Stats.DAMAGE, 14)),
    //DUELIST("Duelist", Color.MAGENTA, new Stats()
    //        .put(Stats.MAX_ENERGY, 125)
    //        .put(Stats.MAX_HEALTH, 750)
    //        .put(Stats.DAMAGE, 21)
    //        .put(Stats.HEALTH_PER_TURN, 10)),
    GUNSLINGER("Gunslinger", Color.of(255, 110, 0), new Stats()
            .put(MAX_ENERGY, 125)
            .put(MAX_HEALTH, 1090)
            .put(ATTACK_POWER, 19)
            .put(HEALTH_PER_TURN, 12))
            {
                private static final String VAR_BARRAGE_COOLDOWN = "barrage_cooldown";
                private static final String VAR_BARRAGE_COUNT = "barrage_count";
                private static final String VAR_ROLL_COOLDOWN = "roll_cooldown";
                private static final String VAR_DEADEYE_COOLDOWN = "deadeye_cooldown";
                private static final String VAR_FIRST_ATTACKED = "already_first_attacked";

                public Cooldown getBarrageCooldown(GameMemberVars vars)
                {
                    if (!vars.has(this, VAR_BARRAGE_COOLDOWN))
                        setBarrageCooldown(vars, new Cooldown(GUNSLINGER_BARRAGE_COOLDOWN));
                    return vars.get(this, VAR_BARRAGE_COOLDOWN, Cooldown.class);
                }

                public void setBarrageCooldown(GameMemberVars vars, Cooldown barrageCooldown)
                {
                    vars.put(this, VAR_BARRAGE_COOLDOWN, barrageCooldown);
                }

                public int getBarrageCount(GameMemberVars vars)
                {
                    if (!vars.has(this, VAR_BARRAGE_COUNT))
                        setBarrageCount(vars, 0);
                    return vars.get(this, VAR_BARRAGE_COUNT, Integer.class);
                }

                public void setBarrageCount(GameMemberVars vars, int barrageCount)
                {
                    vars.put(this, VAR_BARRAGE_COUNT, barrageCount);
                }

                public Cooldown getRollCooldown(GameMemberVars vars)
                {
                    if (!vars.has(this, VAR_ROLL_COOLDOWN))
                        setRollCooldown(vars, new Cooldown(GUNSLINGER_ROLL_COOLDOWN));
                    return vars.get(this, VAR_ROLL_COOLDOWN, Cooldown.class);
                }

                public void setRollCooldown(GameMemberVars vars, Cooldown rollCooldown)
                {
                    vars.put(this, VAR_ROLL_COOLDOWN, rollCooldown);
                }

                public Cooldown getDeadeyeCooldown(GameMemberVars vars)
                {
                    if (!vars.has(this, VAR_DEADEYE_COOLDOWN))
                        setDeadeyeCooldown(vars, new Cooldown(GUNSLINGER_DEADEYE_COOLDOWN));
                    return vars.get(this, VAR_DEADEYE_COOLDOWN, Cooldown.class);
                }

                public void setDeadeyeCooldown(GameMemberVars vars, Cooldown deadeyeCooldown)
                {
                    vars.put(this, VAR_DEADEYE_COOLDOWN, deadeyeCooldown);
                }

                public boolean getFirstAttacked(GameMemberVars vars)
                {
                    if (!vars.has(this, VAR_FIRST_ATTACKED))
                        setFirstAttacked(vars, false);
                    return vars.get(this, VAR_FIRST_ATTACKED, Boolean.class);
                }

                public void setFirstAttacked(GameMemberVars vars, boolean firstAttacked)
                {
                    vars.put(this, VAR_FIRST_ATTACKED, firstAttacked);
                }

                @Override
                public String getDescription()
                {
                    return "The first Attack per turn always Crits and deals __" + percent(GUNSLINGER_PASSIVE_AP_RATIO) + " Attack Power__ bonus damage.";
                }

                @Override
                public Skill[] getSkills()
                {
                    return new Skill[]{new BarrageSkill(), new RollSkill(), new DeadeyeSkill()};
                }

                @Override
                public DamageEvent attackOut(DamageEvent event)
                {
                    GameMemberVars vars = event.actor.getVars();
                    Stats stats = event.actor.getStats();

                    if (!getFirstAttacked(vars))
                    {
                        setFirstAttacked(vars, true);
                        event.crit = true;
                        event.bonus += stats.get(ATTACK_POWER) * GUNSLINGER_PASSIVE_AP_RATIO;
                    }

                    return event;
                }

                @Override
                public String onTurnStart(GameMember member)
                {
                    GameMemberVars vars = member.getVars();
                    setFirstAttacked(vars, false);
                    return null;
                }

                class BarrageSkill extends Skill
                {
                    public BarrageSkill()
                    {
                        super(GUNSLINGER, GUNSLINGER_BARRAGE_COOLDOWN, GUNSLINGER_BARRAGE_ENERGY_COST);
                    }

                    @Override
                    public GameAction act(Game game, GameMember member)
                    {
                        return new BarrageAction(game.getRandomTarget(member));
                    }

                    @Override
                    public String getName()
                    {
                        return "Barrage";
                    }

                    @Override
                    public String getDescription()
                    {
                        return "Fire **" + GUNSLINGER_BARRAGE_SHOTS + "** shots, each dealing __" + GUNSLINGER_BARRAGE_DAMAGE + "__ + __" + percent(GUNSLINGER_BARRAGE_AP_RATIO) + " Attack Power__ + __" + percent(GUNSLINGER_BARRAGE_SP_RATIO) + " Skill Power__." +
                                "\nShots can crit and apply on-hit effects at __25%__ power.";
                    }
                }

                class BarrageAction implements GameAction
                {
                    private final GameMember target;

                    public BarrageAction(GameMember target)
                    {
                        this.target = target;
                    }

                    @Override
                    public String act(GameMember actor)
                    {
                        GameMemberVars vars = actor.getVars();
                        Cooldown barrageCooldown = getBarrageCooldown(vars);
                        Stats stats = actor.getStats();

                        barrageCooldown.start(stats.getInt(COOLDOWN_REDUCTION));

                        setBarrageCooldown(vars, barrageCooldown);

                        List<String> output = new ArrayList<>();
                        int barrageCount = getBarrageCount(vars);
                        for (int i = 0; i < GUNSLINGER_BARRAGE_SHOTS; i++)
                            if (target.isAlive())
                            {
                                DamageEvent event = new DamageEvent(actor, target);
                                event.onHitScale = .25f;
                                event.damage += stats.get(ATTACK_POWER) * GUNSLINGER_BARRAGE_AP_RATIO;
                                event.damage += stats.get(SKILL_POWER) * GUNSLINGER_BARRAGE_SP_RATIO;
                                event = actor.skill(event);
                                event = actor.crit(event);
                                event = actor.hit(event);

                                if (!event.cancelled)
                                    barrageCount++;

                                output.add(actor.damage(event, Emote.GUN, "Barrage"));
                            }
                        setBarrageCount(vars, barrageCount);
                        output.add(0, Emote.SKILL + "**" + actor.getUsername() + "** used **Barrage**!");

                        return Util.joinNonEmpty("\n", output);
                    }

                    @Override
                    public int getEnergy()
                    {
                        return 25;
                    }
                }

                class RollSkill extends Skill
                {
                    public RollSkill()
                    {
                        super(GUNSLINGER, GUNSLINGER_ROLL_COOLDOWN, 0);
                    }

                    @Override
                    public GameAction act(Game game, GameMember member)
                    {
                        return new RollAction();
                    }

                    @Override
                    public String getName()
                    {
                        return "Roll";
                    }

                    @Override
                    public String getDescription()
                    {
                        return "End the turn and gain __" + percent(GUNSLINGER_ROLL_DODGE) + "__ + __" + percentRaw(GUNSLINGER_ROLL_SP_RATIO) + " Skill Power__ Dodge.";
                    }
                }

                class RollAction implements GameAction
                {
                    @Override
                    public String act(GameMember actor)
                    {
                        GameMemberVars vars = actor.getVars();
                        Game game = actor.getGame();
                        Cooldown rollCooldown = getRollCooldown(vars);
                        Stats stats = actor.getStats();

                        rollCooldown.start(stats.getInt(COOLDOWN_REDUCTION));
                        setRollCooldown(vars, rollCooldown);

                        float dodge = GUNSLINGER_ROLL_DODGE + (stats.get(SKILL_POWER) * GUNSLINGER_ROLL_SP_RATIO);

                        actor.addBuff(new RollingBuff(actor, dodge), Emote.NINJA);
                        actor.setEnergy(0);

                        return Emote.NINJA + "**" + actor.getUsername() + "** used **Roll**, gaining __" + percent(dodge) + "__ Dodge!";
                    }

                    @Override
                    public int getEnergy()
                    {
                        return 0;
                    }
                }

                class RollingBuff extends Buff
                {
                    public RollingBuff(GameMember source, float power)
                    {
                        super("Rolling", false, source, 2, power);
                    }

                    @Override
                    public String getStatus(GameMember member)
                    {
                        return "Rolling: " + percent(getPower()) + " bonus Dodge";
                    }

                    @Override
                    public Stats getStats()
                    {
                        return new Stats()
                                .put(DODGE, getPower());
                    }
                }

                class DeadeyeSkill extends Skill
                {
                    public DeadeyeSkill()
                    {
                        super(GUNSLINGER, GUNSLINGER_DEADEYE_COOLDOWN, GUNSLINGER_DEADEYE_ENERGY_COST);
                    }

                    @Override
                    public GameAction act(Game game, GameMember member)
                    {
                        return new DeadeyeAction(game.getRandomTarget(member));
                    }

                    @Override
                    public String getName()
                    {
                        return "Deadeye";
                    }

                    @Override
                    public String getDescription()
                    {
                        return "Deal __" + GUNSLINGER_DEADEYE_DAMAGE + "__ + __" + percent(GUNSLINGER_DEADEYE_AP_RATIO) + " Attack Power__." +
                                "\nHas a __" + percent(GUNSLINGER_DEADEYE_CHANCE) + "__ chance to **Jackpot**, increased by __" + percentRaw(GUNSLINGER_DEADEYE_JACKPOT_BARRAGE_INCREASE) + "__ per Barrage shot hit." +
                                "\nJackpot instead deals __" + percent(GUNSLINGER_DEADEYE_JACKPOT_RATIO) + "__ of the target's missing health." +
                                "\nDeadeye can crit.";
                    }
                }

                class DeadeyeAction implements GameAction
                {
                    private final GameMember target;

                    public DeadeyeAction(GameMember target)
                    {
                        this.target = target;
                    }

                    @Override
                    public String act(GameMember actor)
                    {
                        GameMemberVars vars = actor.getVars();
                        Game game = actor.getGame();
                        Cooldown deadeyeCooldown = getDeadeyeCooldown(vars);
                        int barrageCount = getBarrageCount(vars);
                        Stats stats = actor.getStats();

                        deadeyeCooldown.start(stats.getInt(COOLDOWN_REDUCTION));
                        setDeadeyeCooldown(vars, deadeyeCooldown);

                        DamageEvent event = new DamageEvent(actor, target);
                        List<String> output = new ArrayList<>();

                        boolean jackpot = false;
                        float jackpotRand = Util.RANDOM.nextFloat();
                        if (jackpotRand <= GUNSLINGER_DEADEYE_CHANCE + (barrageCount * GUNSLINGER_DEADEYE_JACKPOT_BARRAGE_INCREASE))
                        {
                            event.damage += Math.max(1, (event.target.getStats().get(MAX_HEALTH) - event.target.getHealth()) * GUNSLINGER_DEADEYE_JACKPOT_RATIO);
                            jackpot = true;
                        } else
                        {
                            event.damage += GUNSLINGER_DEADEYE_DAMAGE;
                            event.damage += stats.get(ATTACK_POWER) * GUNSLINGER_DEADEYE_AP_RATIO;
                        }

                        event = actor.crit(event);
                        event = actor.skill(event);

                        output.add(actor.damage(event, Emote.GUN, "Deadeye"));
                        output.add(0, Emote.SKILL + "**" + actor.getUsername() + "** used **Deadeye**!" + (jackpot ? " **JACKPOT**!" : ""));

                        return Util.joinNonEmpty("\n", output);
                    }

                    @Override
                    public int getEnergy()
                    {
                        return 50;
                    }
                }
            };
    //PHASEBREAKER("Phasebreaker", Color.of(0, 255, 191), new Stats()
    //        .put(Stats.MAX_ENERGY, 125)
    //        .put(Stats.MAX_HEALTH, 750)
    //        .put(Stats.DAMAGE, 18)
    //        .put(Stats.HEALTH_PER_TURN, 10)),
    //THIEF("Thief", Color.YELLOW, new Stats()
    //        .put(Stats.MAX_ENERGY, 150)
    //        .put(Stats.MAX_HEALTH, 735)
    //        .put(Stats.DAMAGE, 17)
    //        .put(Stats.HEALTH_PER_TURN, 8)
    //        .put(Stats.CRIT_CHANCE, 0.2f)
    //        .put(Stats.CRIT_DAMAGE, -1 * .2f)),
    //WARRIOR("Warrior", Color.CYAN, new Stats()
    //        .put(MAX_ENERGY, 125)
    //        .put(MAX_HEALTH, 775)
    //        .put(ATTACK_POWER, 22)
    //        .put(HEALTH_PER_TURN, 12)) {
    //    private static final String VAR_PASSIVE_COUNT = "passive_count";
    //    private static final String VAR_BASH_COOLDOWN = "bash_cooldown";
//
    //    public Stacker getPassiveCount(GameMemberVars vars) {
    //        if (!vars.has(this, VAR_PASSIVE_COUNT))
    //            setPassiveCount(vars, new Stacker(WARRIOR_PASSIVE_LIMIT));
    //        return vars.get(this, VAR_PASSIVE_COUNT, Stacker.class);
    //    }
//
    //    public void setPassiveCount(GameMemberVars vars, Stacker passiveCount) {
    //        vars.put(this, VAR_PASSIVE_COUNT, passiveCount);
    //    }
//
    //    public Cooldown getBashCooldown(GameMemberVars vars) {
    //        if (!vars.has(this, VAR_BASH_COOLDOWN))
    //            setBashCooldown(vars, new Cooldown(WARRIOR_BASH_COOLDOWN));
    //        return vars.get(this, VAR_BASH_COOLDOWN, Cooldown.class);
    //    }
//
    //    public void setBashCooldown(GameMemberVars vars, Cooldown bashCooldown) {
    //        vars.put(this, VAR_BASH_COOLDOWN, bashCooldown);
    //    }
//
    //    @Override
    //    public String getDescription() {
    //        return "Every **" + WARRIOR_PASSIVE_LIMIT + "** Attacks, deal __" + percent(WARRIOR_PASSIVE_AP_RATIO) + " Attack Power__ bonus damage.";
    //    }
//
    //    @Override
    //    public String[] getTopic(GameMember member) {
    //        GameMemberVars vars = member.getVars();
    //        Stacker passiveCount = getPassiveCount(vars);
    //        Cooldown bashCooldown = getBashCooldown(vars);
//
    //        return new String[]{
    //                "Bonus: **" + passiveCount.getCurrent() + " / 3**",
    //                bashCooldown.isDone()
    //                        ? "Bash: **Ready**"
    //                        : "Bash: **" + bashCooldown.getCurrent() + "** turns"};
    //    }
//
    //    @Override
    //    public Skill[] getSkills() {
    //        return new Skill[] { new BashSkill() };
    //    }
//
    //    @Override
    //    public String onTurnStart(GameMember member) {
    //        GameMemberVars vars = member.getVars();
    //        Cooldown bashCooldown = getBashCooldown(vars);
    //        boolean bashReady = bashCooldown.count();
    //        boolean bashNotify = bashReady && bashCooldown.tryNotify();
//
    //        setBashCooldown(vars, bashCooldown);
//
    //        if (bashReady && bashNotify)
    //            return Emote.INFO + "**" + member.getUsername() + "**'s Bash is ready to use.";
//
    //        return null;
    //    }
//
    //    @Override
    //    public DamageEvent attackOut(DamageEvent event) {
    //        GameMemberVars vars = event.actor.getVars();
    //        Stacker passiveCount = getPassiveCount(vars);
//
    //        if (passiveCount.stack()) {
    //            Stats stats = event.actor.getStats();
    //            event.bonus += stats.get(ATTACK_POWER) * WARRIOR_PASSIVE_AP_RATIO;
    //            passiveCount.reset();
    //        }
//
    //        setPassiveCount(vars, passiveCount);
//
    //        return event;
    //    }
//
    //    class BashCommand implements Command {
    //        @Override
    //        public void execute(Message message, String[] args) {
    //            User author = message.getAuthor().orElse(null);
    //            MessageChannel channel = message.getChannel().block();
    //            Game game = Enigma.getInstance().getPlayer(author).getGame();
    //            GameMember member = game.getMember(author);
//
    //            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
    //                message.delete().subscribe();
    //                if (member.hasBuff(SilenceDebuff.class))
    //                    Util.sendFailure(channel, "You cannot **Bash** while silenced.");
    //                else {
    //                    GameMemberVars vars = member.getVars();
    //                    Cooldown bashCooldown = getBashCooldown(vars);
//
    //                    if (!bashCooldown.isDone())
    //                        Util.sendFailure(channel, "**Bash** is on cooldown for **" + bashCooldown.getCurrent() + "** more turns.");
    //                    else
    //                        member.act(new BashAction(game.getRandomTarget(member)));
    //                }
    //            }
    //        }
//
    //        @Override
    //        public String getName() {
    //            return "bash";
    //        }
//
    //        @Override
    //        public String getDescription() {
    //            return "Break the target's shield and resist, then deal __" + percent(WARRIOR_BASH_AP_RATIO) + " Attack Power__ + __" + percent(WARRIOR_BASH_SP_RATIO) + " Skill Power__." +
    //                    "\nBash stacks Warrior's passive, but doesn't activate it.";
    //        }
    //    }
//
    //    class BashAction implements GameAction {
    //        private final GameMember target;
//
    //        public BashAction(GameMember target) {
    //            this.target = target;
    //        }
//
    //        @Override
    //        public String act(GameMember actor) {
    //            GameMemberVars vars = actor.getVars();
    //            Cooldown bashCooldown = getBashCooldown(vars);
    //            Stacker passiveCount = getPassiveCount(vars);
//
    //            bashCooldown.start();
    //            passiveCount.stack();
//
    //            setBashCooldown(vars, bashCooldown);
    //            setPassiveCount(vars, passiveCount);
//
    //            DamageEvent event = new DamageEvent(actor, target);
    //            Stats actorStats = event.actor.getStats();
    //            Stats targetStats = event.target.getStats();
//
    //            event.target.setDefensive(false);
//
    //            if (targetStats.get(Stats.RESIST) > 0) {
    //                targetStats.put(Stats.RESIST, 0);
    //                event.output.add(Emote.SHIELD + " It broke their resist!");
    //            }
//
    //            if (event.target.hasShield()) {
    //                event.target.setShield(0);
    //                event.output.add(Emote.SHIELD + " It broke their shield!");
    //            }
//
    //            event.damage += actorStats.get(ATTACK_POWER) * WARRIOR_BASH_AP_RATIO;
    //            event.damage += actorStats.get(SKILL_POWER) * WARRIOR_BASH_SP_RATIO;
//
    //            event = event.actor.skill(event);
//
    //            return actor.damage(event, Emote.KNIFE, "Bash");
    //        }
//
    //        @Override
    //        public int getEnergy() {
    //            return 25;
    //        }
    //    }
    //};

    private final String name;
    private final Color color;
    private final Stats stats;

    Unit(String name, Color color, Stats stats)
    {
        this.name = name;
        this.color = color;
        this.stats = stats;
    }

    public static Unit fromName(String query)
    {
        for (Unit unit : values())
        {
            String name = unit.getName().toLowerCase();

            if (query.equals(name) || (query.length() >= 3 && name.startsWith(query)))
                return unit;
        }
        return null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public abstract String getDescription();

    public Color getColor()
    {
        return color;
    }

    public Stats getStats()
    {
        return stats;
    }

    public abstract Skill[] getSkills();

    public EmbedCreateSpec format()
    {
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();

        embed.color(getColor());
        embed.description("## " + getName() + "\n" + getDescription() + "\n\u1CBC\u1CBC");

        for (Skill skill : getSkills())
            embed.addField("`>" + skill.getName() + "` - Cooldown: **" +
                    (skill.hasCooldown() ? skill.getBaseCooldown() : "None") +
                    "** - Energy Cost: **" +
                    (skill.hasEnergyCost() ? skill.getEnergyCost() : "Free") +
                    "**", skill.getDescription(), false);

        return embed.build();
    }

    public EmbedCreateSpec formatStats()
    {
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        List<String> desc = new ArrayList<>();

        desc.add("## " + getName() + " Stats");
        desc.add("Health: **" + stats.getInt(MAX_HEALTH) + "** (+**" + stats.getInt(HEALTH_PER_TURN) + "**/turn)");
        desc.add("Attack Power: **" + stats.getInt(ATTACK_POWER) + "**");
        desc.add("Energy: **" + stats.getInt(MAX_ENERGY) + "**");
        if (stats.get(CRIT_CHANCE) > 0)
            desc.add("Critical Chance: **" + percent(stats.get(CRIT_CHANCE)) + "**");
        if (stats.get(LIFE_STEAL) > 0)
            desc.add("Life Steal: **" + percent(stats.get(LIFE_STEAL)) + "**");

        embed.color(getColor());
        embed.description(String.join("\n", desc));

        return embed.build();
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
