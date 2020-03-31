package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.listener.CommandListener;
import com.oopsjpeg.enigma.storage.Player;
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
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.oopsjpeg.enigma.game.Stats.*;

public class Game {
    public static final int PICKING = 0;
    public static final int PLAYING = 1;
    @Getter private final Enigma instance;
    @Getter private final TextChannel channel;
    @Getter private final GameMode mode;
    @Getter private final List<GameMember> members;
    @Getter private final CommandListener commandListener;

    @Getter @Setter private List<GameAction> actions = new ArrayList<>();
    @Getter @Setter private LocalDateTime lastAction = LocalDateTime.now();
    @Getter @Setter private int afkNotifier = 0;

    @Getter @Setter private int gameState = PICKING;
    @Getter @Setter private int turnCount = 0;
    @Getter @Setter private int turnIndex = -1;

    public Game(Enigma instance, GameMode mode, List<Player> players) {
        this.instance = instance;
        this.mode = mode;

        channel = instance.getGuild().createTextChannel(c -> c.setName("game")).block();

        Snowflake roleId = getGuild().getEveryoneRole().block().getId();
        channel.addRoleOverwrite(roleId, PermissionOverwrite.forRole(roleId,
                PermissionSet.none(), PermissionSet.of(Permission.VIEW_CHANNEL))).block();
        players.forEach(p -> channel.addMemberOverwrite(Snowflake.of(p.getId()), PermissionOverwrite.forMember(Snowflake.of(p.getId()),
                PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none())).block());

        commandListener = new CommandListener(instance,
                instance.getSettings().get(Settings.GAME_PREFIX),
                GameCommand.values(), channel);
        instance.addListener(commandListener);

        members = players.stream().map(p -> new GameMember(this, p)).collect(Collectors.toList());
        Collections.shuffle(members);

        nextTurn();
    }

    public void nextTurn() {
        List<String> output = new ArrayList<>();

        // Handle turn ending
        if (gameState == PLAYING) {
            // On turn end
            output.addAll(getCurrentMember().getData().stream().map(e -> e.onTurnEnd(getCurrentMember())).collect(Collectors.toList()));
            // On defend
            if (turnCount >= 1 && getCurrentMember().getStats().get(ENERGY) > 0 && !getCurrentMember().hasData(Silence.class))
                output.add(getCurrentMember().defend());
            // Check buffs
            getCurrentMember().getBuffs().stream().filter(b -> b.turn() == 0).forEach(buff -> {
                output.add(Emote.INFO + "**" + getCurrentMember().getUsername() + "**'s " + buff.getName() + " has expired.");
                getCurrentMember().getData().remove(buff);
            });
        }

        // Start next turn
        turnIndex++;
        // Reset turn index at max
        if (turnIndex >= members.size()) {
            turnIndex = 0;
            // Start game once all players have picked
            if (gameState == PICKING && members.stream().allMatch(GameMember::hasUnit))
                gameState = PLAYING;
        }

        if (gameState == PICKING) {
            // First pick message
            if (turnIndex == 0) {
                String playerList = getPlayers().stream().map(Player::getUsername).collect(Collectors.joining(", "));
                output.add(Emote.ATTACK + "Welcome to **" + mode.getName() + "**! (" + playerList + ")");
            }
            output.add("[**" + getCurrentMember().getMention() + ", pick your unit!**]");
            output.add("Check " + instance.getUnitsChannel().getMention() + " to view units, then pick with one with `"
                    + commandListener.getPrefix() + GameCommand.PICK.getAliases()[0] + "`.");
        } else if (gameState == PLAYING) {
            GameMember member = getCurrentMember();
            member.heal(member.getStats().get(HEALTH_PER_TURN) * (member.isDefensive() ? 2 : 1), null, false);
            member.getStats().add(GOLD, mode.handleGold(125 + turnCount));
            member.getStats().add(GOLD, member.getStats().get(GOLD_PER_TURN));
            member.getStats().put(ENERGY, member.getUnit().getStats().get(ENERGY));
            member.getStats().add(ENERGY, member.getStats().get(ENERGY_PER_TURN));
            member.setDefensive(false);

            turnCount++;

            output.add("[**" + member.getMention() + ", it's your turn!**]");
            output.add("Open this channel's description to review your stats and items.");

            // On turn start
            output.addAll(member.getData().stream().map(e -> e.onTurnStart(member)).collect(Collectors.toList()));
            // Low health warning
            if (member.getStats().get(HEALTH) < member.getStats().get(MAX_HEALTH) * 0.2f)
                output.add(Emote.WARN + "**" + member.getUsername() + "** is critically low on health.");

            member.getStats().put(SHIELD, 0);
        }

        channel.createMessage(Util.joinNonEmpty(output)).block();
        setTopic(getCurrentMember());
    }

    public void setTopic(GameMember member) {
        channel.edit(c -> c.setTopic(getTopic(member))).block();
    }

    public String getTopic(GameMember member) {
        if (gameState == PICKING)
            return member.getUsername() + " is picking their unit.";
        else {
            List<String> output = new ArrayList<>();
            output.add(member.getUnit().getName() + " " + member.getMention() + " (" + turnCount + ") -\n");
            output.add("Gold: **" + Util.comma(member.getStats().getInt(GOLD)) + "**");
            output.add("Health: **" + member.getStats().getInt(HEALTH) + " / " + member.getStats().getInt(MAX_HEALTH)
                    + "** (+**" + member.getStats().getInt(HEALTH_PER_TURN) + "**/t)");
            output.add("Energy: **" + member.getStats().getInt(ENERGY) + "**");
            output.addAll(Arrays.asList(member.getUnit().getTopic()));
            output.add("Items: **" + member.getItems() + "**");
            return Util.joinNonEmpty(output);
        }
    }

    public Guild getGuild() {
        return channel.getGuild().block();
    }

    public GameMember getMember(User user) {
        return members.stream()
                .filter(m -> m.getUser().equals(user))
                .findAny().orElse(null);
    }

    public GameMember getCurrentMember() {
        return members.get(turnIndex);
    }

    public GameMember getRandomTarget(GameMember exclude) {
        List<GameMember> targets = getAlive().stream().filter(m -> !m.equals(exclude)).collect(Collectors.toList());
        return targets.get(Util.RANDOM.nextInt(targets.size()));
    }

    public List<User> getUsers() {
        return members.stream().map(GameMember::getUser).collect(Collectors.toList());
    }

    public List<Player> getPlayers() {
        return members.stream().map(GameMember::getPlayer).collect(Collectors.toList());
    }

    public List<GameMember> getAlive() {
        return members.stream().filter(GameMember::isAlive).collect(Collectors.toList());
    }

    public List<GameMember> getDead() {
        return members.stream().filter(m -> !m.isAlive()).collect(Collectors.toList());
    }

    public void notifyAfk() {
        afkNotifier++;
        if (afkNotifier == 4)
            channel.createMessage(Emote.WARN + getCurrentMember().getMention() + ", you have around **4** minutes " +
                    "to make an action, otherwise you will **forfeit due to AFKing**.").block();
        else if (afkNotifier >= 8)
            channel.createMessage(getCurrentMember().lose()).block();
    }
}
