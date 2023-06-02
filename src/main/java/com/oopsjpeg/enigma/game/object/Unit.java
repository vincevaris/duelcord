package com.oopsjpeg.enigma.game.object;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.*;
import com.oopsjpeg.enigma.game.buff.SilenceDebuff;
import com.oopsjpeg.enigma.util.Cooldown;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Stacker;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
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
            .put(MAX_HEALTH, 720)
            .put(ATTACK_POWER, 22)
            .put(HEALTH_PER_TURN, 9)) {

        private Stacker getPotencyTurn(GameMemberVars vars) {
            if (!vars.has(this, "potency_turn"))
                setPotencyTurn(vars, new Stacker(ASSASSIN_POTENCY_TURN_LIMIT));
            return vars.get(this, "potency_turn", Stacker.class);
        }

        private void setPotencyTurn(GameMemberVars vars, Stacker potencyTurn) {
            vars.put(this, "potency_turn", potencyTurn);
        }

        private int getPotency(GameMemberVars vars) {
            if (!vars.has(this, "potency"))
                setPotency(vars, 0);
            return vars.get(this, "potency", Integer.class);
        }

        private void setPotency(GameMemberVars vars, int potency) {
            vars.put(this, "potency", potency);
        }

        private Stacker getSlashCount(GameMemberVars vars) {
            if (!vars.has(this, "slash_count"))
                setSlashCount(vars, new Stacker(ASSASSIN_SLASH_COUNT_LIMIT));
            return vars.get(this, "slash_count", Stacker.class);
        }

        private void setSlashCount(GameMemberVars vars, Stacker slashCount) {
            vars.put(this, "slash_count", slashCount);
        }

        private boolean getSlashedAlready(GameMemberVars vars) {
            if (!vars.has(this, "slashed_already"))
                setSlashedAlready(vars, false);
            return vars.get(this, "slashed_already", Boolean.class);
        }

        private void setSlashedAlready(GameMemberVars vars, boolean slashedAlready) {
            vars.put(this, "slashed_already", slashedAlready);
        }

        @Override
        public String getDescription() {
            return "**" + percent(ASSASSIN_DAMAGE_TO_POTENCY) + "** of damage dealt last turn is stored as **Potency**." +
                    "\nThis can occur **" + ASSASSIN_POTENCY_TURN_LIMIT + "** times until **Potency** is consumed.";
        }

        @Override
        public String[] getTopic(GameMember member) {
            GameMemberVars vars = member.getVars();
            Stacker slashCount = getSlashCount(vars);
            int potency = getPotency(vars);

            return new String[]{
                    "Slash: **" + slashCount.getCurrent() + " / " + slashCount.getMax() + "**",
                    "Potency: **" + potency + "**"};
        }

        @Override
        public Command[] getCommands() {
            return new Command[]{new SlashCommand()};
        }

        @Override
        public String onTurnEnd(GameMember member) {
            GameMemberVars vars = member.getVars();
            Stacker potencyCount = getPotencyTurn(vars);
            boolean potencyFull = potencyCount.stack();
            setSlashedAlready(vars, false);
            setPotencyTurn(vars, potencyCount);

            if (potencyFull)
                return Emote.KNIFE + "**" + member.getUsername() + "'s Potency** is at max capacity.";

            return null;
        }

        @Override
        public DamageEvent attackOut(DamageEvent event) {
            // Assassin potency stacking
            GameMemberVars vars = event.actor.getVars();
            Stacker potencyCount = getPotencyTurn(vars);

            if (!potencyCount.isDone()) {
                int potency = getPotency(vars);
                potency += event.damage * ASSASSIN_DAMAGE_TO_POTENCY;
                setPotency(vars, potency);
            }

            return event;
        }

        class SlashCommand implements Command {
            @Override
            public void execute(Message message, String[] args) {
                User author = message.getAuthor().orElse(null);
                MessageChannel channel = message.getChannel().block();
                Game game = Enigma.getInstance().getPlayer(author).getGame();
                GameMember member = game.getMember(author);

                if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                    message.delete().block();
                    GameMemberVars vars = member.getVars();
                    if (member.hasBuff(SilenceDebuff.class))
                        Util.sendFailure(channel, "You cannot **Slash** while silenced.");
                    else if (getSlashedAlready(vars))
                        Util.sendFailure(channel, "You can only use **Slash** once per turn.");
                    else
                        member.act(new SlashAction(game.getRandomTarget(member)));
                }
            }

            @Override
            public String getName() {
                return "slash";
            }

            @Override
            public String getDescription() {
                return "Deal __" + percent(ASSASSIN_SLASH_AP_RATIO) + " Attack Power__ + __" + percent(ASSASSIN_SLASH_SP_RATIO) + " Skill Power__."
                        + "\nEvery **" + ASSASSIN_SLASH_COUNT_LIMIT + "** uses, **Silence** and consume **Potency** to deal bonus damage equal to it."
                        + "\nSlash damage doesn't count towards Potency.";
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

                setSlashedAlready(vars, true);

                DamageEvent event = new DamageEvent(actor.getGame(), actor, target);
                event.damage += stats.get(ATTACK_POWER) * ASSASSIN_SLASH_AP_RATIO;
                event.damage += stats.get(SKILL_POWER) * ASSASSIN_SLASH_SP_RATIO;

                Stacker slashCount = getSlashCount(vars);
                if (slashCount.stack()) {
                    Stacker potencyTurn = getPotencyTurn(vars);

                    event.damage += getPotency(vars);
                    event.output.add(target.buff(new SilenceDebuff(actor)));

                    slashCount.reset();
                    potencyTurn.reset();

                    setPotency(vars, 0);
                    setPotencyTurn(vars, potencyTurn);
                }

                setSlashCount(vars, slashCount);

                event = event.actor.hit(event);
                event = event.actor.crit(event);
                event = event.actor.ability(event);

                return actor.damage(event, Emote.KNIFE, "Slash");
            }

            @Override
            public int getEnergy() {
                return 25;
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
        public Command[] getCommands() {
            return new Command[]{ new BarrageCommand(), new RollCommand(), new DeadeyeCommand() };
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

            List<String> output = new ArrayList<>();

            Cooldown barrageCooldown = getBarrageCooldown(vars);
            boolean barrageReady = barrageCooldown.count();
            boolean barrageNotify = barrageReady && barrageCooldown.tryNotify();
            setBarrageCooldown(vars, barrageCooldown);

            if (barrageReady && barrageNotify)
                output.add(Emote.INFO + "**" + member.getUsername() + "**'s Barrage is ready to use.");

            Cooldown rollCooldown = getRollCooldown(vars);
            boolean rollReady = rollCooldown.count();
            boolean rollNotify = rollReady && rollCooldown.tryNotify();
            setRollCooldown(vars, rollCooldown);

            if (rollReady && rollNotify)
                output.add(Emote.INFO + "**" + member.getUsername() + "**'s Roll is ready to use.");

            Cooldown deadeyeCooldown = getDeadeyeCooldown(vars);
            boolean deadeyeReady = deadeyeCooldown.count();
            boolean deadeyeNotify = deadeyeReady && deadeyeCooldown.tryNotify();
            setDeadeyeCooldown(vars, deadeyeCooldown);

            if (deadeyeReady && deadeyeNotify)
                output.add(Emote.INFO + "**" + member.getUsername() + "**'s Deadeye is ready to use.");

            return output.isEmpty() ? null : String.join("\n", output);
        }

        class BarrageCommand implements Command {
            @Override
            public void execute(Message message, String[] args) {
                User author = message.getAuthor().orElse(null);
                MessageChannel channel = message.getChannel().block();
                Game game = Enigma.getInstance().getPlayer(author).getGame();
                GameMember member = game.getMember(author);

                if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                    message.delete().block();
                    if (member.hasBuff(SilenceDebuff.class))
                        Util.sendFailure(channel, "You cannot **Barrage** while silenced.");
                    else {
                        GameMemberVars vars = member.getVars();
                        Cooldown barrageCooldown = getBarrageCooldown(vars);

                        if (!barrageCooldown.isDone())
                            Util.sendFailure(channel, "**Barrage** is on cooldown for **" + barrageCooldown.getCurrent() + "** more turns.");
                        else
                            member.act(new BarrageAction(game.getRandomTarget(member)));
                    }
                }
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

                barrageCooldown.start();

                setBarrageCooldown(vars, barrageCooldown);

                List<String> output = new ArrayList<>();
                int barrageCount = getBarrageCount(vars);
                for (int i = 0; i < GUNSLINGER_BARRAGE_SHOTS; i++)
                    if (target.isAlive()) {
                        DamageEvent event = new DamageEvent(actor.getGame(), actor, target);
                        event.damage += stats.get(ATTACK_POWER) * GUNSLINGER_BARRAGE_AP_RATIO;
                        event.damage += stats.get(SKILL_POWER) * GUNSLINGER_BARRAGE_SP_RATIO;
                        actor.crit(event);
                        actor.hit(event);

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

        class RollCommand implements Command {
            @Override
            public void execute(Message message, String[] args) {
                User author = message.getAuthor().orElse(null);
                MessageChannel channel = message.getChannel().block();
                Game game = Enigma.getInstance().getPlayer(author).getGame();
                GameMember member = game.getMember(author);

                if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                    message.delete().block();
                    if (member.hasBuff(SilenceDebuff.class))
                        Util.sendFailure(channel, "You cannot **Roll** while silenced.");
                    else {
                        GameMemberVars vars = member.getVars();
                        Cooldown rollCooldown = getRollCooldown(vars);

                        if (!rollCooldown.isDone())
                            Util.sendFailure(channel, "**Roll** is on cooldown for **" + rollCooldown.getCurrent() + "** more turns.");
                        else
                            member.act(new RollAction());
                    }
                }
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

                rollCooldown.start();
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

        class DeadeyeCommand implements Command {
            @Override
            public void execute(Message message, String[] args) {
                User author = message.getAuthor().orElse(null);
                MessageChannel channel = message.getChannel().block();
                Game game = Enigma.getInstance().getPlayer(author).getGame();
                GameMember member = game.getMember(author);

                if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                    message.delete().block();
                    if (member.hasBuff(SilenceDebuff.class))
                        Util.sendFailure(channel, "You cannot **Deadeye** while silenced.");
                    else {
                        GameMemberVars vars = member.getVars();
                        Cooldown deadeyeCooldown = getDeadeyeCooldown(vars);

                        if (!deadeyeCooldown.isDone())
                            Util.sendFailure(channel, "**Deadeye** is on cooldown for **" + deadeyeCooldown.getCurrent() + "** more turns.");
                        else
                            member.act(new DeadeyeAction(game.getRandomTarget(member)));
                    }
                }
            }

            @Override
            public String getName() {
                return "deadeye";
            }

            @Override
            public String getDescription() {
                return "Deal __" + GUNSLINGER_DEADEYE_DAMAGE + "__ + __" + percent(GUNSLINGER_DEADEYE_AP_RATIO) + " Attack Power__." +
                        "\nHas a __" + percent(GUNSLINGER_DEADEYE_CHANCE) + "__ chance to **Jackpot**, increased by __" + percentRaw(GUNSLINGER_DEADEYE_JACKPOT_BARRAGE_INCREASE) + "__ per Barrage shot hit." +
                        "\nJackpot deals __" + percent(GUNSLINGER_DEADEYE_JACKPOT_RATIO) + "__ of the target's missing health." +
                        "\nDeadshot can crit.";
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

                deadeyeCooldown.start();
                setDeadeyeCooldown(vars, deadeyeCooldown);

                DamageEvent event = new DamageEvent(actor.getGame(), actor, target);
                event.damage += GUNSLINGER_DEADEYE_DAMAGE;
                event.damage += stats.get(ATTACK_POWER) * GUNSLINGER_DEADEYE_AP_RATIO;
                actor.crit(event);

                List<String> output = new ArrayList<>();

                boolean jackpot = false;
                float jackpotRand = Util.RANDOM.nextFloat();
                if (jackpotRand <= GUNSLINGER_DEADEYE_CHANCE + (barrageCount * GUNSLINGER_DEADEYE_JACKPOT_BARRAGE_INCREASE)) {
                    event.bonus += Math.max(1, (event.target.getStats().get(MAX_HEALTH) - event.target.getHealth()) * GUNSLINGER_DEADEYE_JACKPOT_RATIO);
                    jackpot = true;
                }

                output.add(actor.damage(event, Emote.GUN, "Deadeye"));
                output.add(0, Emote.USE + "**" + actor.getUsername() + "** used **Deadeye**!" + (jackpot ? " **JACKPOT**!" : ""));

                return Util.joinNonEmpty("\n", output);
            }

            @Override
            public int getEnergy() {
                return 50;
            }
        }
    },
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
    WARRIOR("Warrior", Color.CYAN, new Stats()
            .put(MAX_ENERGY, 125)
            .put(MAX_HEALTH, 775)
            .put(ATTACK_POWER, 22)
            .put(HEALTH_PER_TURN, 12)) {
        private static final String VAR_PASSIVE_COUNT = "passive_count";
        private static final String VAR_BASH_COOLDOWN = "bash_cooldown";

        public Stacker getPassiveCount(GameMemberVars vars) {
            if (!vars.has(this, VAR_PASSIVE_COUNT))
                setPassiveCount(vars, new Stacker(WARRIOR_PASSIVE_LIMIT));
            return vars.get(this, VAR_PASSIVE_COUNT, Stacker.class);
        }

        public void setPassiveCount(GameMemberVars vars, Stacker passiveCount) {
            vars.put(this, VAR_PASSIVE_COUNT, passiveCount);
        }

        public Cooldown getBashCooldown(GameMemberVars vars) {
            if (!vars.has(this, VAR_BASH_COOLDOWN))
                setBashCooldown(vars, new Cooldown(WARRIOR_BASH_COOLDOWN));
            return vars.get(this, VAR_BASH_COOLDOWN, Cooldown.class);
        }

        public void setBashCooldown(GameMemberVars vars, Cooldown bashCooldown) {
            vars.put(this, VAR_BASH_COOLDOWN, bashCooldown);
        }

        @Override
        public String getDescription() {
            return "Every **" + WARRIOR_PASSIVE_LIMIT + "** Attacks, deal __" + percent(WARRIOR_PASSIVE_AP_RATIO) + " Attack Power__ bonus damage.";
        }

        @Override
        public String[] getTopic(GameMember member) {
            GameMemberVars vars = member.getVars();
            Stacker passiveCount = getPassiveCount(vars);
            Cooldown bashCooldown = getBashCooldown(vars);

            return new String[]{
                    "Bonus: **" + passiveCount.getCurrent() + " / 3**",
                    bashCooldown.isDone()
                            ? "Bash: **Ready**"
                            : "Bash: **" + bashCooldown.getCurrent() + "** turns"};
        }

        @Override
        public Command[] getCommands() {
            return new Command[]{new BashCommand()};
        }

        @Override
        public String onTurnStart(GameMember member) {
            GameMemberVars vars = member.getVars();
            Cooldown bashCooldown = getBashCooldown(vars);
            boolean bashReady = bashCooldown.count();
            boolean bashNotify = bashReady && bashCooldown.tryNotify();

            setBashCooldown(vars, bashCooldown);

            if (bashReady && bashNotify)
                return Emote.INFO + "**" + member.getUsername() + "**'s Bash is ready to use.";

            return null;
        }

        @Override
        public DamageEvent attackOut(DamageEvent event) {
            GameMemberVars vars = event.actor.getVars();
            Stacker passiveCount = getPassiveCount(vars);

            if (passiveCount.stack()) {
                Stats stats = event.actor.getStats();
                event.bonus += stats.get(ATTACK_POWER) * WARRIOR_PASSIVE_AP_RATIO;
                passiveCount.reset();
            }

            setPassiveCount(vars, passiveCount);

            return event;
        }

        class BashCommand implements Command {
            @Override
            public void execute(Message message, String[] args) {
                User author = message.getAuthor().orElse(null);
                MessageChannel channel = message.getChannel().block();
                Game game = Enigma.getInstance().getPlayer(author).getGame();
                GameMember member = game.getMember(author);

                if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                    message.delete().block();
                    if (member.hasBuff(SilenceDebuff.class))
                        Util.sendFailure(channel, "You cannot **Bash** while silenced.");
                    else {
                        GameMemberVars vars = member.getVars();
                        Cooldown bashCooldown = getBashCooldown(vars);

                        if (!bashCooldown.isDone())
                            Util.sendFailure(channel, "**Bash** is on cooldown for **" + bashCooldown.getCurrent() + "** more turns.");
                        else
                            member.act(new BashAction(game.getRandomTarget(member)));
                    }
                }
            }

            @Override
            public String getName() {
                return "bash";
            }

            @Override
            public String getDescription() {
                return "Break the target's shield and resist, then deal __" + percent(WARRIOR_BASH_AP_RATIO) + " Attack Power__ + __" + percent(WARRIOR_BASH_SP_RATIO) + " Skill Power__." +
                        "\nBash stacks Warrior's passive, but doesn't activate it.";
            }
        }

        class BashAction implements GameAction {
            private final GameMember target;

            public BashAction(GameMember target) {
                this.target = target;
            }

            @Override
            public String act(GameMember actor) {
                GameMemberVars vars = actor.getVars();
                Cooldown bashCooldown = getBashCooldown(vars);
                Stacker passiveCount = getPassiveCount(vars);

                bashCooldown.start();
                passiveCount.stack();

                setBashCooldown(vars, bashCooldown);
                setPassiveCount(vars, passiveCount);

                DamageEvent event = new DamageEvent(actor.getGame(), actor, target);
                Stats actorStats = event.actor.getStats();
                Stats targetStats = event.target.getStats();

                event.target.setDefensive(false);

                if (targetStats.get(Stats.RESIST) > 0) {
                    targetStats.put(Stats.RESIST, 0);
                    event.output.add(Emote.SHIELD + " It broke their resist!");
                }

                if (event.target.hasShield()) {
                    event.target.setShield(0);
                    event.output.add(Emote.SHIELD + " It broke their shield!");
                }

                event.damage += actorStats.get(ATTACK_POWER) * WARRIOR_BASH_AP_RATIO;
                event.damage += actorStats.get(SKILL_POWER) * WARRIOR_BASH_SP_RATIO;

                event = event.actor.ability(event);

                return actor.damage(event, Emote.KNIFE, "Bash");
            }

            @Override
            public int getEnergy() {
                return 25;
            }
        }
    };

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

    public Command[] getCommands() {
        return new Command[0];
    }

    public EmbedCreateSpec format() {
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();

        embed.color(getColor());
        embed.description("## " + getName() + "\n" + getDescription() + "\n\u1CBC\u1CBC");

        for (Command cmd : getCommands())
            embed.addField("`>" + cmd.getName() + "`", cmd.getDescription(), false);

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
