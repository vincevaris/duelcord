package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.CrippleDebuff;
import com.oopsjpeg.enigma.game.buff.SilenceDebuff;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.util.ArrayList;
import java.util.List;

import static com.oopsjpeg.enigma.game.Stats.*;
import static com.oopsjpeg.enigma.game.unit.UnitConstants.*;
import static com.oopsjpeg.enigma.util.Util.percent;
import static com.oopsjpeg.enigma.util.Util.percentRaw;

public enum Unit implements GameObject {
    ASSASSIN("Assassin", Color.of(0, 69, 255), new Stats()
            .put(MAX_ENERGY, 125)
            .put(MAX_HEALTH, 940)
            .put(ATTACK_POWER, 22)
            .put(HEALTH_PER_TURN, 9)) {
        private static final String VAR_SLASH_COOLDOWN = "slash_cooldown";
        private static final String VAR_CLOAK_COOLDOWN = "cloak_cooldown";
        private static final String VAR_GOUGE_COOLDOWN = "gouge_cooldown";

        private static final String VAR_POTENCY = "potency";
        private static final String VAR_SLASH_COUNT = "slash_count";

        public Cooldown getSlashCooldown(GameMemberVars vars) {
            if (!vars.has(this, VAR_SLASH_COOLDOWN))
                setSlashCooldown(vars, new Cooldown(ASSASSIN_SLASH_COOLDOWN));
            return vars.get(this, VAR_SLASH_COOLDOWN, Cooldown.class);
        }

        public void setSlashCooldown(GameMemberVars vars, Cooldown slashCooldown) {
            vars.put(this, VAR_SLASH_COOLDOWN, slashCooldown);
        }

        public Cooldown getCloakCooldown(GameMemberVars vars) {
            if (!vars.has(this, VAR_CLOAK_COOLDOWN))
                setCloakCooldown(vars, new Cooldown(ASSASSIN_CLOAK_COOLDOWN));
            return vars.get(this, VAR_CLOAK_COOLDOWN, Cooldown.class);
        }

        public void setCloakCooldown(GameMemberVars vars, Cooldown cloakCooldown) {
            vars.put(this, VAR_CLOAK_COOLDOWN, cloakCooldown);
        }

        public Cooldown getGougeCooldown(GameMemberVars vars) {
            if (!vars.has(this, VAR_GOUGE_COOLDOWN))
                setGougeCooldown(vars, new Cooldown(ASSASSIN_GOUGE_COOLDOWN));
            return vars.get(this, VAR_GOUGE_COOLDOWN, Cooldown.class);
        }

        public void setGougeCooldown(GameMemberVars vars, Cooldown gougeCooldown) {
            vars.put(this, VAR_GOUGE_COOLDOWN, gougeCooldown);
        }

        public int getPotency(GameMemberVars vars) {
            if (!vars.has(this, VAR_POTENCY))
                setPotency(vars, 0);
            return vars.get(this, VAR_POTENCY, Integer.class);
        }

        public void setPotency(GameMemberVars vars, int potency) {
            vars.put(this, VAR_POTENCY, potency);
        }

        public Stacker getSlashCount(GameMemberVars vars) {
            if (!vars.has(this, VAR_SLASH_COUNT))
                setSlashCount(vars, new Stacker(ASSASSIN_SLASH_MAX));
            return vars.get(this, VAR_SLASH_COUNT, Stacker.class);
        }

        public void setSlashCount(GameMemberVars vars, Stacker slashCount) {
            vars.put(this, VAR_SLASH_COUNT, slashCount);
        }

        @Override
        public Skill[] getSkills() {
            return new Skill[] { new SlashSkill(), new CloakSkill(), new GougeSkill() };
        }

        @Override
        public String getDescription() {
            return "__" + percent(ASSASSIN_POTENCY_RATIO) + "__ of damage dealt with Attacks is stored as **Potency**, up to __" + ASSASSIN_POTENCY_MAX + "__ + __" + percent(ASSASSIN_POTENCY_SP_RATIO) + " Spell Power__.\n" +
                    "When you defend, gain __" + percent(ASSASSIN_STEALTH_DODGE) + "__ Dodge.";
        }

        @Override
        public String[] getTopic(GameMember member) {
            GameMemberVars vars = member.getVars();
            Stacker slashCount = getSlashCount(vars);
            Cooldown slashCooldown = getSlashCooldown(vars);
            Cooldown cloakCooldown = getCloakCooldown(vars);
            Cooldown gougeCooldown = getGougeCooldown(vars);

            Stats stats = member.getStats();
            return new String[] {
                    "Potency: " + getPotency(vars) + " / " + getMaxPotency(stats),
                    (slashCooldown.isDone()
                            ? "Slash: Ready"
                            : "Slash: " + slashCount + "/" + ASSASSIN_SLASH_MAX + " - " + slashCooldown.getCurrent() + " turns"),
                    (cloakCooldown.isDone()
                            ? "Cloak: Ready"
                            : "Cloak: " + cloakCooldown.getCurrent() + " turns"),
                    (gougeCooldown.isDone()
                            ? "Gouge: Ready"
                            : "Gouge: " + gougeCooldown.getCurrent() + " turns")
            };
        }

        @Override
        public String onDefend(GameMember member) {
            member.getBuffs().add(new StealthBuff(member, ASSASSIN_STEALTH_DODGE));
            return Emote.SHIELD + "**" + member.getUsername() + "** is in stealth, gaining __" + percent(ASSASSIN_STEALTH_DODGE) + "__ Dodge.";
        }

        @Override
        public DamageEvent attackOut(DamageEvent event) {
            GameMember actor = event.actor;
            GameMemberVars vars = actor.getVars();
            int potency = getPotency(vars);
            int potencyToAdd = Math.round(event.damage * ASSASSIN_POTENCY_RATIO);

            potency = Util.limit(potency + potencyToAdd, 0, getMaxPotency(actor.getStats()));

            setPotency(vars, potency);

            return event;
        }

        @Override
        public DamageEvent dodgeMe(DamageEvent event) {
            return super.dodgeMe(event);
        }

        public int getMaxPotency(Stats stats) {
            return Math.round(ASSASSIN_POTENCY_MAX + (stats.get(SKILL_POWER) * ASSASSIN_POTENCY_SP_RATIO));
        }

        class StealthBuff extends Buff {
            public StealthBuff(GameMember source, float power) {
                super("Stealth", false, source, 2, power);
            }

            @Override
            public Stats getStats() {
                return new Stats()
                        .put(DODGE, getPower());
            }
        }

        class SlashSkill extends Skill {
            public SlashSkill() {
                super(ASSASSIN, ASSASSIN_SLASH_COOLDOWN);
            }

            @Override
            public String getName() {
                return "slash";
            }

            @Override
            public String getDescription() {
                return "Deal __" + percent(ASSASSIN_SLASH_AP_RATIO) + " Attack Power__ + __" + percent(ASSASSIN_SLASH_SP_RATIO) + " Spell Power__. Can crit.\n" +
                        "Every **" + ASSASSIN_SLASH_MAX + "** uses, **Silence** the target and consume **Potency** to deal bonus damage equal to it.";
            }

            @Override
            public GameAction act(Game game, GameMember actor) {
                return new SlashAction(game.getRandomTarget(actor));
            }
        }

        class SlashAction implements GameAction {
            private final GameMember target;

            public SlashAction(GameMember target) {
                this.target = target;
            }

            @Override
            public String act(GameMember actor) {
                GameMemberVars vars = actor.getVars();
                Stats stats = actor.getStats();

                Cooldown slashCooldown = getSlashCooldown(vars);
                slashCooldown.start(stats.getInt(COOLDOWN_REDUCTION));
                setSlashCooldown(vars, slashCooldown);

                DamageEvent event = new DamageEvent(actor, target);

                event.damage += stats.get(ATTACK_POWER) * ASSASSIN_SLASH_AP_RATIO;
                event.damage += stats.get(SKILL_POWER) * ASSASSIN_SLASH_SP_RATIO;

                Stacker slashCount = getSlashCount(vars);

                // Third slash
                if (slashCount.stack()) {
                    int potency = getPotency(vars);
                    event.bonus += potency;

                    event.output.add(event.target.buff(new SilenceDebuff(actor)));

                    slashCount.reset();
                    setPotency(vars, 0);
                }

                setSlashCount(vars, slashCount);

                event = actor.crit(event);
                event = actor.ability(event);

                return target.damage(event, Emote.KNIFE, "Slash");
            }

            @Override
            public int getEnergy() {
                return ASSASSIN_SLASH_COST;
            }
        }

        class CloakSkill extends Skill {
            public CloakSkill() {
                super(ASSASSIN, ASSASSIN_CLOAK_COOLDOWN);
            }

            @Override
            public String getName() {
                return "cloak";
            }

            @Override
            public String getDescription() {
                return "End the turn and gain __" + percent(ASSASSIN_CLOAK_DODGE) + "__ Dodge. The next dodge generates __" + ASSASSIN_CLOAK_POTENCY + "__ + __" + percent(ASSASSIN_CLOAK_POTENCY_SP_RATIO) + " Spell Power__ Potency and dispels this buff.";
            }

            @Override
            public GameAction act(Game game, GameMember actor) {
                return new CloakAction();
            }
        }

        class CloakAction implements GameAction {
            @Override
            public String act(GameMember actor) {
                GameMemberVars vars = actor.getVars();
                Cooldown cloakCooldown = getCloakCooldown(vars);
                cloakCooldown.start(actor.getStats().getInt(COOLDOWN_REDUCTION));
                setCloakCooldown(vars, cloakCooldown);

                actor.getBuffs().add(new CloakBuff(actor, ASSASSIN_CLOAK_DODGE));
                actor.setEnergy(0);

                return Emote.USE + "**" + actor.getUsername() + "** used **Cloak**!";
            }

            @Override
            public int getEnergy() {
                return 0;
            }
        }

        class CloakBuff extends Buff {
            public CloakBuff(GameMember source, float power) {
                super("Cloak", false, source, 2, power);
            }

            @Override
            public DamageEvent dodgeMe(DamageEvent event) {
                GameMemberVars vars = event.actor.getVars();
                Stats stats = event.actor.getStats();

                int potency = getPotency(vars);
                int potencyToAdd = Math.round(ASSASSIN_CLOAK_POTENCY + (stats.get(SKILL_POWER) * ASSASSIN_CLOAK_POTENCY_SP_RATIO));

                potency = Util.limit(potency + potencyToAdd, 0, getMaxPotency(stats));

                setPotency(vars, potency);

                event.actor.removeBuff(this);
                return event;
            }

            @Override
            public Stats getStats() {
                return new Stats()
                        .put(DODGE, ASSASSIN_CLOAK_DODGE);
            }
        }

        class GougeSkill extends Skill {
            public GougeSkill() {
                super(ASSASSIN, ASSASSIN_GOUGE_COOLDOWN);
            }

            @Override
            public String getName() {
                return "gouge";
            }

            @Override
            public String getDescription() {
                return "Your next **2** Attacks this turn deal __" + percent(ASSASSIN_GOUGE_DAMAGE_AP_RATIO) + " Attack Power__ bonus damage and have __" + percent(ASSASSIN_GOUGE_CRIPPLE_CHANCE) + "__ chance to **Cripple** the target by __" + percent(ASSASSIN_GOUGE_CRIPPLE_AMOUNT) + "__.\n" +
                        "Cripple can stack.";
            }

            @Override
            public GameAction act(Game game, GameMember actor) {
                return new GougeAction();
            }
        }

        class GougeAction implements GameAction {
            @Override
            public String act(GameMember actor) {
                GameMemberVars vars = actor.getVars();
                Cooldown gougeCooldown = getGougeCooldown(vars);
                gougeCooldown.start(actor.getStats().getInt(COOLDOWN_REDUCTION));
                setGougeCooldown(vars, gougeCooldown);

                int damage = Math.round(actor.getStats().get(ATTACK_POWER) * ASSASSIN_GOUGE_DAMAGE_AP_RATIO);
                actor.getBuffs().add(new GougeBuff(actor, 2, damage, ASSASSIN_GOUGE_CRIPPLE_CHANCE, ASSASSIN_GOUGE_CRIPPLE_AMOUNT));
                return Emote.USE + "**" + actor.getUsername() + "** used **Gouge**!";
            }

            @Override
            public int getEnergy() {
                return ASSASSIN_GOUGE_COST;
            }
        }

        class GougeBuff extends Buff {
            private final int maxAttacks;
            private final int damage;
            private final float crippleChance;
            private final float crippleAmount;

            private int attacks;

            public GougeBuff(GameMember source, int maxAttacks, int damage, float crippleChance, float crippleAmount) {
                super("Gouge", false, source, 1, 0);
                this.maxAttacks = maxAttacks;
                this.damage = damage;
                this.crippleChance = crippleChance;
                this.crippleAmount = crippleAmount;
            }

            @Override
            public DamageEvent attackOut(DamageEvent event) {
                event.bonus += damage;

                float crippleRand = Util.RANDOM.nextFloat();
                if (crippleRand <= crippleChance)
                    event.output.add(event.target.buff(new CrippleDebuff(event.actor, 1, crippleAmount)));

                attacks++;

                if (attacks >= maxAttacks) event.actor.removeBuff(this);

                return event;
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
            .put(HEALTH_PER_TURN, 12)) {
        private static final String VAR_BARRAGE_COOLDOWN = "barrage_cooldown";
        private static final String VAR_BARRAGE_COUNT = "barrage_count";
        private static final String VAR_ROLL_COOLDOWN = "roll_cooldown";
        private static final String VAR_DEADEYE_COOLDOWN = "deadeye_cooldown";
        private static final String VAR_FIRST_ATTACKED = "already_first_attacked";

        public Cooldown getBarrageCooldown(GameMemberVars vars) {
            if (!vars.has(this, VAR_BARRAGE_COOLDOWN))
                setBarrageCooldown(vars, new Cooldown(GUNSLINGER_BARRAGE_COOLDOWN));
            return vars.get(this, VAR_BARRAGE_COOLDOWN, Cooldown.class);
        }

        public void setBarrageCooldown(GameMemberVars vars, Cooldown barrageCooldown) {
            vars.put(this, VAR_BARRAGE_COOLDOWN, barrageCooldown);
        }

        public int getBarrageCount(GameMemberVars vars) {
            if (!vars.has(this, VAR_BARRAGE_COUNT))
                setBarrageCount(vars, 0);
            return vars.get(this, VAR_BARRAGE_COUNT, Integer.class);
        }

        public void setBarrageCount(GameMemberVars vars, int barrageCount) {
            vars.put(this, VAR_BARRAGE_COUNT, barrageCount);
        }

        public Cooldown getRollCooldown(GameMemberVars vars) {
            if (!vars.has(this, VAR_ROLL_COOLDOWN))
                setRollCooldown(vars, new Cooldown(GUNSLINGER_ROLL_COOLDOWN));
            return vars.get(this, VAR_ROLL_COOLDOWN, Cooldown.class);
        }

        public void setRollCooldown(GameMemberVars vars, Cooldown rollCooldown) {
            vars.put(this, VAR_ROLL_COOLDOWN, rollCooldown);
        }

        public Cooldown getDeadeyeCooldown(GameMemberVars vars) {
            if (!vars.has(this, VAR_DEADEYE_COOLDOWN))
                setDeadeyeCooldown(vars, new Cooldown(GUNSLINGER_DEADEYE_COOLDOWN));
            return vars.get(this, VAR_DEADEYE_COOLDOWN, Cooldown.class);
        }

        public void setDeadeyeCooldown(GameMemberVars vars, Cooldown deadeyeCooldown) {
            vars.put(this, VAR_DEADEYE_COOLDOWN, deadeyeCooldown);
        }

        public boolean getFirstAttacked(GameMemberVars vars) {
            if (!vars.has(this, VAR_FIRST_ATTACKED))
                setFirstAttacked(vars, false);
            return vars.get(this, VAR_FIRST_ATTACKED, Boolean.class);
        }

        public void setFirstAttacked(GameMemberVars vars, boolean firstAttacked) {
            vars.put(this, VAR_FIRST_ATTACKED, firstAttacked);
        }

        @Override
        public String getDescription() {
            return "The first Attack per turn always Crits and deals __" + percent(GUNSLINGER_PASSIVE_AP_RATIO) + " Attack Power__ bonus damage.";
        }

        @Override
        public String[] getTopic(GameMember member) {
            GameMemberVars vars = member.getVars();
            Cooldown barrageCooldown = getBarrageCooldown(vars);
            Cooldown rollCooldown = getRollCooldown(vars);
            Cooldown deadeyeCooldown = getDeadeyeCooldown(vars);

            return new String[]{
                    (barrageCooldown.isDone()
                            ? "Barrage: Ready"
                            : "Barrage: " + barrageCooldown.getCurrent() + " turns"),
                    (rollCooldown.isDone()
                            ? "Roll: Ready"
                            : "Roll: " + rollCooldown.getCurrent() + " turns"),
                    (deadeyeCooldown.isDone()
                            ? "Deadeye: Ready"
                            : "Deadeye: " + deadeyeCooldown.getCurrent() + " turns")
            };
        }

        @Override
        public Skill[] getSkills() {
            return new Skill[]{ new BarrageSkill(), new RollSkill(), new DeadeyeSkill() };
        }

        @Override
        public DamageEvent attackOut(DamageEvent event) {
            GameMemberVars vars = event.actor.getVars();
            Stats stats = event.actor.getStats();

            if (!getFirstAttacked(vars)) {
                setFirstAttacked(vars, true);
                event.crit = true;
                event.bonus += stats.get(ATTACK_POWER) * GUNSLINGER_PASSIVE_AP_RATIO;
            }

            return event;
        }

        @Override
        public String onTurnStart(GameMember member) {
            GameMemberVars vars = member.getVars();
            setFirstAttacked(vars, false);
            return null;
        }

        class BarrageSkill extends Skill {
            public BarrageSkill() {
                super(GUNSLINGER, GUNSLINGER_BARRAGE_COOLDOWN);
            }

            @Override
            public GameAction act(Game game, GameMember member) {
                return new BarrageAction(game.getRandomTarget(member));
            }

            @Override
            public String getName() {
                return "barrage";
            }

            @Override
            public String getDescription() {
                return "Fire **" + GUNSLINGER_BARRAGE_SHOTS + "** shots, each dealing __" + GUNSLINGER_BARRAGE_DAMAGE + "__ + __" + percent(GUNSLINGER_BARRAGE_AP_RATIO) + " Attack Power__ + __" + percent(GUNSLINGER_BARRAGE_SP_RATIO) + " Skill Power__." +
                        "\nShots can crit and apply on-hit effects.";
            }
        }

        class BarrageAction implements GameAction {
            private final GameMember target;

            public BarrageAction(GameMember target) {
                this.target = target;
            }

            @Override
            public String act(GameMember actor) {
                GameMemberVars vars = actor.getVars();
                Cooldown barrageCooldown = getBarrageCooldown(vars);
                Stats stats = actor.getStats();

                barrageCooldown.start(stats.getInt(COOLDOWN_REDUCTION));

                setBarrageCooldown(vars, barrageCooldown);

                List<String> output = new ArrayList<>();
                int barrageCount = getBarrageCount(vars);
                for (int i = 0; i < GUNSLINGER_BARRAGE_SHOTS; i++)
                    if (target.isAlive()) {
                        DamageEvent event = new DamageEvent(actor, target);
                        event.damage += stats.get(ATTACK_POWER) * GUNSLINGER_BARRAGE_AP_RATIO;
                        event.damage += stats.get(SKILL_POWER) * GUNSLINGER_BARRAGE_SP_RATIO;
                        event = actor.crit(event);
                        event = actor.hit(event);
                        event = actor.ability(event);

                        if (!event.cancelled)
                            barrageCount++;

                        output.add(actor.damage(event, Emote.GUN, "Barrage"));
                    }
                setBarrageCount(vars, barrageCount);
                output.add(0, Emote.USE + "**" + actor.getUsername() + "** used **Barrage**!");

                return Util.joinNonEmpty("\n", output);
            }

            @Override
            public int getEnergy() {
                return 25;
            }
        }

        class RollSkill extends Skill {
            public RollSkill() {
                super(GUNSLINGER, GUNSLINGER_ROLL_COOLDOWN);
            }

            @Override
            public GameAction act(Game game, GameMember member) {
                return new RollAction();
            }

            @Override
            public String getName() {
                return "roll";
            }

            @Override
            public String getDescription() {
                return "End the turn and gain __" + percent(GUNSLINGER_ROLL_DODGE) + "__ + __" + percentRaw(GUNSLINGER_ROLL_SP_RATIO) + " Skill Power__ Dodge.";
            }
        }

        class RollAction implements GameAction {
            @Override
            public String act(GameMember actor) {
                GameMemberVars vars = actor.getVars();
                Game game = actor.getGame();
                Cooldown rollCooldown = getRollCooldown(vars);
                Stats stats = actor.getStats();

                rollCooldown.start(stats.getInt(COOLDOWN_REDUCTION));
                setRollCooldown(vars, rollCooldown);

                actor.buff(new RollBuff(actor, GUNSLINGER_ROLL_DODGE + (stats.get(SKILL_POWER) * GUNSLINGER_ROLL_SP_RATIO)));
                actor.setEnergy(0);

                return Emote.USE + "**" + actor.getUsername() + "** used **Roll**!";
            }

            @Override
            public int getEnergy() {
                return 0;
            }
        }

        class RollBuff extends Buff {
            public RollBuff(GameMember source, float power) {
                super("Roll", false, source, 2, power);
            }

            @Override
            public Stats getStats() {
                return new Stats()
                        .put(DODGE, getPower());
            }
        }

        class DeadeyeSkill extends Skill {
            public DeadeyeSkill() {
                super(GUNSLINGER, GUNSLINGER_DEADEYE_COOLDOWN);
            }

            @Override
            public GameAction act(Game game, GameMember member) {
                return new DeadeyeAction(game.getRandomTarget(member));
            }

            @Override
            public String getName() {
                return "deadeye";
            }

            @Override
            public String getDescription() {
                return "Deal __" + GUNSLINGER_DEADEYE_DAMAGE + "__ + __" + percent(GUNSLINGER_DEADEYE_AP_RATIO) + " Attack Power__." +
                        "\nHas a __" + percent(GUNSLINGER_DEADEYE_CHANCE) + "__ chance to **Jackpot**, increased by __" + percentRaw(GUNSLINGER_DEADEYE_JACKPOT_BARRAGE_INCREASE) + "__ per Barrage shot hit." +
                        "\nJackpot instead deals __" + percent(GUNSLINGER_DEADEYE_JACKPOT_RATIO) + "__ of the target's missing health." +
                        "\nDeadeye can crit.";
            }
        }

        class DeadeyeAction implements GameAction {
            private final GameMember target;

            public DeadeyeAction(GameMember target) {
                this.target = target;
            }

            @Override
            public String act(GameMember actor) {
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
                if (jackpotRand <= GUNSLINGER_DEADEYE_CHANCE + (barrageCount * GUNSLINGER_DEADEYE_JACKPOT_BARRAGE_INCREASE)) {
                    event.damage += Math.max(1, (event.target.getStats().get(MAX_HEALTH) - event.target.getHealth()) * GUNSLINGER_DEADEYE_JACKPOT_RATIO);
                    jackpot = true;
                } else {
                    event.damage += GUNSLINGER_DEADEYE_DAMAGE;
                    event.damage += stats.get(ATTACK_POWER) * GUNSLINGER_DEADEYE_AP_RATIO;
                }

                event = actor.crit(event);
                event = actor.ability(event);

                output.add(actor.damage(event, Emote.GUN, "Deadeye"));
                output.add(0, Emote.USE + "**" + actor.getUsername() + "** used **Deadeye**!" + (jackpot ? " **JACKPOT**!" : ""));

                return Util.joinNonEmpty("\n", output);
            }

            @Override
            public int getEnergy() {
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
    //                message.delete().block();
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
    //            event = event.actor.ability(event);
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

    Unit(String name, Color color, Stats stats) {
        this.name = name;
        this.color = color;
        this.stats = stats;
    }

    public static Unit fromName(String query) {
        for (Unit unit : values()) {
            String name = unit.getName().toLowerCase();

            if (query.equals(name) || (query.length() >= 3 && name.startsWith(query)))
                return unit;
        }
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public abstract String getDescription();

    public Color getColor() {
        return color;
    }

    public Stats getStats() {
        return stats;
    }

    public Skill[] getSkills() {
        return new Skill[0];
    }

    public EmbedCreateSpec format() {
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();

        embed.color(getColor());
        embed.description("## " + getName() + "\n" + getDescription() + "\n\u1CBC\u1CBC");

        for (Skill skill : getSkills())
            embed.addField("`>" + skill.getName() + "`", skill.getDescription(), false);

        return embed.build();
    }

    public EmbedCreateSpec formatStats() {
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        List<String> desc = new ArrayList<>();

        desc.add("## " + getName() + " Stats");
        desc.add("Health: **" + stats.getInt(MAX_HEALTH) + "** (+**" + stats.getInt(HEALTH_PER_TURN) + "**/turn)");
        desc.add("Attack Power: **" + stats.getInt(ATTACK_POWER) + "**");
        desc.add("Energy: **" + stats.getInt(MAX_ENERGY) + "**");
        if (stats.get(CRIT_CHANCE) > 0)
            desc.add("Critical Chance: **" + Util.percent(stats.get(CRIT_CHANCE)) + "**");
        if (stats.get(LIFE_STEAL) > 0)
            desc.add("Life Steal: **" + Util.percent(stats.get(LIFE_STEAL)) + "**");

        embed.color(getColor());
        embed.description(String.join("\n", desc));

        return embed.build();
    }

    @Override
    public String toString() {
        return getName();
    }
}
