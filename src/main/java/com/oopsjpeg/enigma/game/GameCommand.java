package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.action.AttackAction;
import com.oopsjpeg.enigma.game.action.BuyAction;
import com.oopsjpeg.enigma.game.action.SellAction;
import com.oopsjpeg.enigma.game.action.UseAction;
import com.oopsjpeg.enigma.game.buff.SilenceDebuff;
import com.oopsjpeg.enigma.game.object.Item;
import com.oopsjpeg.enigma.game.object.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum GameCommand implements Command {
    ATTACK("attack") {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().subscribe();
                if (game.getGameState() == GameState.PICKING)
                    Util.sendFailure(channel, "You cannot attack until the game has started.");
                else
                    member.act(new AttackAction(game.getRandomTarget(member)));
            }
        }
    },
    BUY("buy") {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().subscribe();
                if (game.getGameState() == GameState.PICKING)
                    Util.sendFailure(channel, "You cannot buy items until the game has started.");
                else {
                    Item item = Item.fromName(String.join(" ", args));
                    if (item == null)
                        Util.sendFailure(channel, "Invalid item. Please try again.");
                    else if (!item.isBuyable())
                        Util.sendFailure(channel, "That item can't be bought.");
                    else {
                        Build build = item.build(member.getItems());

                        if (!member.hasGold(build.getCost()))
                            Util.sendFailure(channel, "You need **" + member.getGoldDifference(build.getCost()) + "** more gold for a(n) **" + item.getName() + "**.");
                        else if (build.getPostData().size() >= 5)
                            Util.sendFailure(channel, "You do not have enough inventory space for a(n) **" + item.getName() + "**.");
                        else
                            member.act(new BuyAction(build));
                    }
                }
            }
        }
    },
    STATS("stats") {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel())) {
                message.delete().subscribe();
                if (game.getGameState() == GameState.PICKING)
                    Util.sendFailure(channel, "You cannot check stats until the game has started.");
                else {
                    Item item = Item.fromName(String.join(" ", args));
                    if (item != null) {
                        Build build = item.build(member.getItems());
                        Util.send(channel, item.getName() + " (" + build.getCost() + "g)", Util.joinNonEmpty("\n",
                                item.hasBuild() ? "*Build: " + Arrays.toString(item.getBuild()) + "*\n" : null,
                                Util.formatStats(item.getStats()),
                                Util.formatEffects(item.getEffects())));
                    } else {
                        Unit unit = Unit.fromName(String.join(" ", args));
                        if (unit != null)
                            channel.createMessage(unit.format()).subscribe();
                        else
                            Util.sendFailure(channel, "Invalid item/unit name.");
                    }
                }
            }
        }
    },
    END("end") {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().subscribe();
                if (game.getGameState() == GameState.PICKING)
                    Util.sendFailure(channel, "You cannot end your turn until the game has started.");
                else
                    channel.createMessage(game.nextTurn()).subscribe();
            }
        }
    },
    FORFEIT("ff") {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();

            if (channel.equals(game.getChannel())) {
                message.delete().subscribe();
                channel.createMessage(game.getMember(author).lose()).subscribe();
            }
        }
    },
    PICK("pick") {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().subscribe();
                if (game.getGameState() == GameState.PLAYING)
                    Util.sendFailure(channel, "You cannot pick a unit after the game has started.");
                else {
                    String name = String.join(" ", args).toLowerCase();
                    Unit unit = name.equals("random")
                            ? Util.pickRandom(Unit.values())
                            : Unit.fromName(name);
                    if (unit == null)
                        Util.sendFailure(channel, "Invalid unit.");
                    else {
                        member.setUnit(unit);
                        final List<String> output = new ArrayList<>();
                        output.add(Emote.YES + "**" + author.getUsername() + "** will play as **" + unit.getName() + "**!");
                        output.add(game.nextTurn());
                        channel.createMessage(Util.joinNonEmpty("\n", output)).subscribe();
                    }
                }
            }
        }
    },
    REFRESH("refresh") {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();

            if (channel.equals(game.getChannel())) {
                message.delete().subscribe();
                game.updateInfo(game.getMember(author));
            }
        }
    },
    SELL("sell") {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().subscribe();
                if (game.getGameState() == GameState.PICKING)
                    Util.sendFailure(channel, "You cannot sell items until the game has started.");
                else {
                    Item item = Item.fromName(String.join(" ", args));
                    if (item == null)
                        Util.sendFailure(channel, "Invalid item.");
                    else if (!member.getData().contains(item))
                        Util.sendFailure(channel, "You don't have a(n) **" + item.getName() + "**.");
                    else
                        member.act(new SellAction(item));
                }
            }
        }
    },
    USE("use") {
        @Override
        public void execute(Message message, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().subscribe();
                if (game.getGameState() == GameState.PICKING)
                    Util.sendFailure(channel, "You cannot use items until the game has started.");
                else {
                    Item item = Item.fromName(String.join(" ", args));
                    if (item == null)
                        Util.sendFailure(channel, "Invalid item.");
                    else if (!member.getData().contains(item))
                        Util.sendFailure(channel, "You don't have a(n) **" + item.getName() + "**.");
                    else if (!item.canUse(member))
                        Util.sendFailure(channel, "**" + item.getName() + "** can't be used.");
                    else
                        member.act(new UseAction(item));
                }
            }
        }
    };

    private final String name;

    GameCommand(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return "placeholder";
    }
}