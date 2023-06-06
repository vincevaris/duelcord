package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.buff.SilenceDebuff;
import com.oopsjpeg.enigma.game.object.Augment;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.game.object.Effect;
import com.oopsjpeg.enigma.game.object.Skill;
import com.oopsjpeg.enigma.listener.CommandListener;
import com.oopsjpeg.enigma.storage.Player;
import com.oopsjpeg.enigma.util.*;
import discord4j.common.util.Snowflake;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.core.spec.TextChannelCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.oopsjpeg.enigma.game.GameState.*;
import static com.oopsjpeg.enigma.game.Stats.*;
import static com.oopsjpeg.enigma.util.Util.percent;

public class Game
{
    private final Enigma instance;
    private final TextChannel channel;
    private final Message infoMessage;
    private final GameMode mode;
    private final List<GameMember> members;
    private final CommandListener commandListener;
    private final Stacker afkTimer = new Stacker(10);

    private List<GameAction> actions = new ArrayList<>();
    private List<Augment> augments = new ArrayList<>();
    private LocalDateTime lastAction = LocalDateTime.now();

    private GameState gameState = PICKING;
    private int turnCount = 0;
    private int turnIndex = -1;

    public Game(Enigma instance, GameMode mode, List<Player> players)
    {
        this.instance = instance;
        this.mode = mode;

        channel = instance.getGuild().createTextChannel(TextChannelCreateSpec.builder().name("game").build()).block();

        Snowflake roleId = getGuild().getEveryoneRole().block().getId();
        channel.addRoleOverwrite(roleId, PermissionOverwrite.forRole(roleId,
                PermissionSet.none(), PermissionSet.of(Permission.VIEW_CHANNEL))).subscribe();
        players.forEach(p -> channel.addMemberOverwrite(Snowflake.of(p.getId()), PermissionOverwrite.forMember(Snowflake.of(p.getId()),
                PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none())).subscribe());

        infoMessage = channel.createEmbed(EmbedCreateSpec.builder().description("Game information will appear here.").build()).block();
        infoMessage.pin().subscribe();

        commandListener = new CommandListener(instance,
                instance.getSettings().get(Settings.GAME_PREFIX),
                GameCommand.values(), channel);
        instance.addListener(commandListener);

        members = players.stream().map(p -> new GameMember(this, p)).collect(Collectors.toList());
        Collections.shuffle(members);

        channel.createMessage(nextTurn()).subscribe();
    }

    public String nextTurn()
    {
        final List<String> output = new ArrayList<>();

        // Handle turn ending
        if (gameState == PLAYING)
        {
            // On turn end
            output.addAll(getCurrentMember().getData().stream().map(e -> e.onTurnEnd(getCurrentMember())).collect(Collectors.toList()));
            // On defend
            if (turnCount >= 1 && getCurrentMember().hasEnergy() && !getCurrentMember().hasBuff(SilenceDebuff.class))
                output.add(getCurrentMember().defend());
            // Decrement buff timers
            // This used to also add to output, but updateStats() now checks for expired buffs.
            getCurrentMember().getBuffs().forEach(Buff::turn);
            // Update current member's stats
            output.add(getCurrentMember().updateStats());
        }

        // Start next turn
        turnIndex++;
        // Reset turn index at max
        if (turnIndex >= members.size())
        {
            turnIndex = 0;
            // Start game once all players have picked
            if (gameState == PICKING && members.stream().allMatch(GameMember::alreadyPickedUnit))
                gameState = PLAYING;
        }

        if (gameState == PICKING)
        {
            // First pick message
            if (turnIndex == 0)
            {
                String playerList = getPlayers().stream().map(Player::getUsername).collect(Collectors.joining(", "));
                output.add("## " + mode.getName());
                output.add("featuring **" + getMembers().get(0).getUsername() + "** vs. **" + getMembers().get(1).getUsername() + "**!");
            }
            output.add("### " + getCurrentMember().getMention() + "'s Pick");
            output.add("Check " + instance.getUnitsChannel().getMention() + " to view units, then pick with one with `"
                    + commandListener.getPrefix() + GameCommand.PICK.getName() + "`.");
        } else if (gameState == PLAYING)
        {
            GameMember member = getCurrentMember();
            member.heal(member.getStats().get(HEALTH_PER_TURN) * (member.isDefensive() ? 2 : 1), null, false);
            member.giveGold(mode.handleGold(125 + turnCount));
            member.giveGold(member.getStats().getInt(GOLD_PER_TURN));
            member.setEnergy(member.getStats().getInt(MAX_ENERGY));
            member.setDefensive(false);

            turnCount++;

            if (turnCount >= 7 && augments.size() < 1)
            {
                Augment augment = Util.pickRandom(getUnusedAugments());
                output.add("# First Augment");
                output.add(Emote.AUGMENT + "**" + augment.getName() + "** - " + augment.getDescription());
                augments.add(augment);
                augment.start(this);
            }

            if (turnCount >= 18 && augments.size() < 2)
            {
                Augment augment = Util.pickRandom(getUnusedAugments());
                output.add("# Second Augment");
                output.add(Emote.AUGMENT + "**" + augment.getName() + "** - " + augment.getDescription());
                augments.add(augment);
                augment.start(this);
            }

            output.add("### " + member.getMention() + "'s Turn");
            output.add("Open this channel's pinned messages to see your stats.");

            // On turn start
            output.addAll(member.getData().stream().map(e -> e.onTurnStart(member)).collect(Collectors.toList()));
            // Count skill cooldowns
            List<String> readiedSkills = Arrays.stream(member.getUnit().getSkills())
                    .filter(Skill::hasCooldown)
                    .filter(skill ->
                    {
                        Cooldown cooldown = skill.getCooldown(member.getVars());
                        return cooldown.count() && cooldown.tryNotify();
                    })
                    .map(skill -> "**`>" + skill.getName() + "`**")
                    .collect(Collectors.toList());
            if (readiedSkills.size() == 1)
                output.add(Emote.USE + readiedSkills.get(0) + " is ready to use.");
            else if (readiedSkills.size() >= 1)
                output.add(Emote.USE + Util.joinWithAnd(readiedSkills) + " are ready to use.");
            // Low health warning
            if (member.getHealthPercentage() < 0.2f)
                output.add(Emote.WARN + "They're critically low on health.");
            // Update current member's stats
            output.add(getCurrentMember().updateStats());

            member.setShield(0);
        }

        updateInfo(getCurrentMember());

        return Util.joinNonEmpty("\n", output);
    }

    public void updateInfo(GameMember member)
    {
        String info = getInfo(member);
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();

        if (member.alreadyPickedUnit())
        {
            embed.author(member.getUnit().getName() + " (" + member.getUsername() + ")", null, member.getUser().getAvatarUrl());

            if (member.getHealthPercentage() < 0.2f)
                embed.color(Color.RED);
            else if (member.getHealthPercentage() < 0.6f)
                embed.color(Color.YELLOW);
            else
                embed.color(Color.GREEN);
        } else
        {
            embed.author(member.getUsername(), null, member.getUser().getAvatarUrl());
            embed.color(Color.GRAY);
        }

        embed.description(info);

        infoMessage.edit(MessageEditSpec.builder()
                        .addEmbed(embed.build())
                        .build())
                .subscribe();
    }

    public String getInfo(GameMember member)
    {
        if (gameState == PICKING)
            return member.getUsername() + " is picking their unit.";
        else
        {
            Stats stats = member.getStats();

            List<String> baseTopic = new ArrayList<>();
            baseTopic.add("- Health: " + percent(member.getHealthPercentage()) + " (" + member.getHealth() + "/" + stats.getInt(MAX_HEALTH) + ")");
            baseTopic.add("- Gold: " + member.getGold());
            baseTopic.add("- Energy: " + member.getEnergy());
            baseTopic.add("- Items: " + member.getItems());

            // Add unit topic
            List<String> unitTopic = Arrays.stream(member.getUnit().getTopic(member))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // Add buff topics
            List<String> buffTopics = new ArrayList<>();
            for (Buff buff : member.getBuffs())
            {
                Arrays.stream(buff.getTopic(member))
                        .filter(Objects::nonNull)
                        .forEach(buffTopics::add);
            }

            // Add effect topics
            List<String> effectTopics = new ArrayList<>();
            for (Effect effect : member.getEffects())
            {
                Arrays.stream(effect.getTopic(member))
                        .filter(Objects::nonNull)
                        .forEach(effectTopics::add);
            }

            return String.join(" \n", baseTopic) + "\n\n" +
                    String.join(" \n", unitTopic) + "\n\n" +
                    String.join(" \n", buffTopics) + "\n\n" +
                    String.join(" \n", effectTopics);

        }
    }

    public Guild getGuild()
    {
        return channel.getGuild().block();
    }

    public GameMember getMember(User user)
    {
        return members.stream()
                .filter(m -> m.getUser().equals(user))
                .findAny().orElse(null);
    }

    public GameMember getCurrentMember()
    {
        return members.get(turnIndex);
    }

    public GameMember getRandomTarget(GameMember exclude)
    {
        List<GameMember> targets = getAlive().stream().filter(m -> !m.equals(exclude)).collect(Collectors.toList());
        return targets.get(Util.RANDOM.nextInt(targets.size()));
    }

    public List<User> getUsers()
    {
        return members.stream().map(GameMember::getUser).collect(Collectors.toList());
    }

    public List<Player> getPlayers()
    {
        return members.stream().map(GameMember::getPlayer).collect(Collectors.toList());
    }

    public List<GameMember> getAlive()
    {
        return members.stream().filter(GameMember::isAlive).collect(Collectors.toList());
    }

    public List<GameMember> getDead()
    {
        return members.stream().filter(m -> !m.isAlive()).collect(Collectors.toList());
    }

    public GameMember getWinner()
    {
        return gameState == FINISHED ? getAlive().get(0) : null;
    }

    public Enigma getInstance()
    {
        return this.instance;
    }

    public TextChannel getChannel()
    {
        return this.channel;
    }

    public GameMode getMode()
    {
        return this.mode;
    }

    public List<GameMember> getMembers()
    {
        return this.members;
    }

    public CommandListener getCommandListener()
    {
        return this.commandListener;
    }

    public Stacker getAfkTimer()
    {
        return this.afkTimer;
    }

    public List<GameAction> getActions()
    {
        return this.actions;
    }

    public void setActions(List<GameAction> actions)
    {
        this.actions = actions;
    }

    public List<Augment> getAugments()
    {
        return augments;
    }

    public void setAugments(List<Augment> augments)
    {
        this.augments = augments;
    }

    public List<Augment> getUnusedAugments() {
        // Get all augments
        List<Augment> allAugments = Arrays.stream(Augment.values()).collect(Collectors.toList());
        // Remove augments we already have
        allAugments.removeIf(a -> augments.contains(a));
        return allAugments;
    }

    public LocalDateTime getLastAction()
    {
        return this.lastAction;
    }

    public void setLastAction(LocalDateTime lastAction)
    {
        this.lastAction = lastAction;
    }

    public GameState getGameState()
    {
        return this.gameState;
    }

    public void setGameState(GameState state)
    {
        this.gameState = state;
    }

    public int getTurnCount()
    {
        return this.turnCount;
    }

    public void setTurnCount(int turnCount)
    {
        this.turnCount = turnCount;
    }

    public int getTurnIndex()
    {
        return this.turnIndex;
    }

    public void setTurnIndex(int turnIndex)
    {
        this.turnIndex = turnIndex;
    }
}
