package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.Command;
import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.action.AttackAction;
import com.oopsjpeg.enigma.game.action.BuyAction;
import com.oopsjpeg.enigma.game.action.SellAction;
import com.oopsjpeg.enigma.game.action.UseAction;
import com.oopsjpeg.enigma.game.buff.Silence;
import com.oopsjpeg.enigma.game.obj.Item;
import com.oopsjpeg.enigma.game.obj.Unit;
import com.oopsjpeg.enigma.util.Emote;
import com.oopsjpeg.enigma.util.Util;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;

import java.util.Arrays;

public enum GameCommand implements Command {
    ATTACK("attack") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (game.getGameState() == Game.PICKING)
                    Util.sendFailure(channel, "You cannot attack until the game has started.");
                else if (member.hasData(Silence.class))
                    Util.sendFailure(channel, "You cannot attack while silenced.");
                else
                    member.act(new AttackAction(game.getRandomTarget(member)));
            }
        }
    },
    BUY("buy") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (game.getGameState() == Game.PICKING)
                    Util.sendFailure(channel, "You cannot buy items until the game has started.");
                else {
                    Item item = Item.fromName(String.join(" ", args));
                    if (item == null)
                        Util.sendFailure(channel, "Invalid item. Please try again.");
                    else {
                        Build build = item.build(member.getItems());

                        if (member.getStats().getInt(Stats.GOLD) < build.getCost())
                            Util.sendFailure(channel, "You need **" + (build.getCost() - member.getStats().getInt(Stats.GOLD)) + "** more gold for a(n) **" + item.getName() + "**.");
                        else if (build.getPostData().size() >= 4)
                            Util.sendFailure(channel, "You do not have enough inventory space for a(n) **" + item.getName() + "**.");
                        else
                            member.act(new BuyAction(build));
                    }
                }
            }
        }
    },
    CHECK("check", "stats") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel())) {
                message.delete().block();
                if (game.getGameState() == Game.PICKING)
                    Util.sendFailure(channel, "You cannot check until the game has started.");
                else if (args.length == 0)
                    Util.send(channel, game.getTopic(game.getRandomTarget(member)));
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
                            channel.createEmbed(Util.formatUnit(unit)).block();
                        else
                            Util.sendFailure(channel, "Invalid item/unit name.");
                    }
                }
            }
        }
    },
    END("end") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (game.getGameState() == Game.PICKING)
                    Util.sendFailure(channel, "You cannot end your turn until the game has started.");
                else
                    game.nextTurn();
            }
        }
    },
    FORFEIT("forfeit", "ff") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();

            if (channel.equals(game.getChannel())) {
                message.delete().block();
                channel.createMessage(game.getMember(author).lose()).block();
            }
        }
    },
    PICK("pick") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (game.getGameState() == Game.PLAYING)
                    Util.sendFailure(channel, "You cannot pick a unit after the game has started.");
                else {
                    String name = String.join(" ", args);
                    Unit unit = name.equalsIgnoreCase("random") ? Unit.values()[Util.RANDOM.nextInt(Unit.values().length)]
                            : Unit.fromName(String.join(" ", args));
                    if (unit == null)
                        Util.sendFailure(channel, "Invalid unit.");
                    else if (game.getMembers().stream().anyMatch(m -> unit.equals(m.getUnit())))
                        Util.sendFailure(channel, "That unit was already chosen.");
                    else {
                        member.setUnit(unit);
                        channel.createMessage(Emote.YES + "**" + author.getUsername() + "** has picked **" + unit.getName() + "**.").block();
                        game.nextTurn();
                    }
                }
            }
        }
    },
    REFRESH("refresh") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();

            if (channel.equals(game.getChannel())) {
                message.delete().block();
                game.setTopic(game.getMember(author));
            }
        }
    },
    SELL("sell") {
        @Override
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (game.getGameState() == Game.PICKING)
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
        public void execute(Message message, String alias, String[] args) {
            User author = message.getAuthor().orElse(null);
            MessageChannel channel = message.getChannel().block();
            Game game = Enigma.getInstance().getPlayer(author).getGame();
            GameMember member = game.getMember(author);

            if (channel.equals(game.getChannel()) && member.equals(game.getCurrentMember())) {
                message.delete().block();
                if (game.getGameState() == Game.PICKING)
                    Util.sendFailure(channel, "You cannot use items until the game has started.");
                else {
                    Item item = Item.fromName(String.join(" ", args));
                    if (item == null)
                        Util.sendFailure(channel, "Invalid item.");
                    else if (!member.getData().contains(item))
                        Util.sendFailure(channel, "You don't have a(n) **" + item.getName() + "**.");
                    else if (!item.canUse(member))
                        Util.sendFailure(channel, "**" + item.getName() + "** can't be used.");
                    else if (item.getCooldown() != null && !item.getCooldown().count())
                        Util.sendFailure(channel, "**" + item.getName() + "** is on cooldown for **" + item.getCooldown().getCurrent() + "** more turn(s).");
                    else
                        member.act(new UseAction(item));
                }
            }
        }
    };

    private final String[] aliases;

    GameCommand(String... aliases) {
        this.aliases = aliases;
    }

    public String[] getAliases() {
        return this.aliases;
    }
}
