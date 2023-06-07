package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.buff.SilencedDebuff;
import com.oopsjpeg.enigma.game.object.Buff;
import com.oopsjpeg.enigma.game.object.Distortion;
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
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.oopsjpeg.enigma.game.GameState.*;
import static com.oopsjpeg.enigma.game.Stats.*;

public class Game
{
    private final Enigma instance;
    private final TextChannel channel;
    private final Message statusMessage;
    private final GameMode mode;
    private final List<GameMember> members;
    private final CommandListener commandListener;
    private final Stacker afkTimer = new Stacker(10);

    private List<GameAction> actions = new ArrayList<>();
    private List<Distortion> distortions = new ArrayList<>();
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

        statusMessage = channel.createEmbed(EmbedCreateSpec.builder().description("Game status will appear here.").build()).block();
        statusMessage.pin().subscribe();

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
            if (turnCount >= 1 && getCurrentMember().hasEnergy() && !getCurrentMember().hasBuff(SilencedDebuff.class))
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

            if (turnCount >= 6 && distortions.size() < 1)
            {
                Distortion distortion = Util.pickRandom(getUnusedDistortions());
                output.add("# First Distortion");
                output.add(Emote.DISTORTION + "**" + distortion.getName() + "** - " + distortion.getDescription());
                distortions.add(distortion);
                distortion.start(this);
            }

            if (turnCount >= 17 && distortions.size() < 2)
            {
                Distortion distortion = Util.pickRandom(getUnusedDistortions());
                output.add("# Second Distortion");
                output.add(Emote.DISTORTION + "**" + distortion.getName() + "** - " + distortion.getDescription());
                distortions.add(distortion);
                distortion.start(this);
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
                output.add(Emote.REFRESH + readiedSkills.get(0) + " is ready to use.");
            else if (readiedSkills.size() >= 1)
                output.add(Emote.REFRESH + Util.joinWithAnd(readiedSkills) + " are ready to use.");
            // Low health warning
            if (member.getHealthPercentage() < 0.2f)
                output.add(Emote.WARN + "**" + member.getUsername() + "** is critically low on health.");
            // Update current member's stats
            output.add(getCurrentMember().updateStats());

            member.setShield(0);
        }

        updateStatus();

        return Util.joinNonEmpty("\n", output);
    }

    public void updateStatus()
    {
        List<EmbedCreateSpec> statuses = getNonCurrentMembers().stream()
                .map(GameMember::getStatus)
                .collect(Collectors.toList());
        statuses.add(0, getCurrentMember().getStatus());

        statusMessage.edit(MessageEditSpec.builder()
                        .embeds(statuses)
                        .build())
                .subscribe();
    }

    public List<GameMember> getNonCurrentMembers()
    {
        return getMembers().stream()
                .filter(member -> !getCurrentMember().equals(member))
                .collect(Collectors.toList());
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

    public List<Distortion> getDistortions()
    {
        return distortions;
    }

    public void setDistortions(List<Distortion> distortions)
    {
        this.distortions = distortions;
    }

    public List<Distortion> getUnusedDistortions() {
        // Get all distortions
        List<Distortion> allDistortions = Arrays.stream(Distortion.values()).collect(Collectors.toList());
        // Remove distortions we already have
        allDistortions.removeIf(a -> distortions.contains(a));
        return allDistortions;
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
