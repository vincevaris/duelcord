package com.oopsjpeg.enigma;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Util {
    private static final Random random = new Random();

    public static int randInt(int max) {
        return random.nextInt(max);
    }

    public static int randInt(int min, int max) {
        return min + random.nextInt(max - min);
    }

    public static float randFloat() {
        return random.nextFloat();
    }

    public static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    public static int calcMaxXp(int level) {
        return (int) Math.round(Math.pow(level * 118, 1.07f) + 155);
    }

    public static void changeTopic(IChannel channel, String topic) {
        RequestBuffer.request(() -> {
            if (channel.getModifiedPermissions(Enigma.getClient().getOurUser())
                    .contains(Permissions.MANAGE_CHANNELS))
                channel.changeTopic(topic);
        });
    }

    public static void bulkDelete(IChannel channel, List<IMessage> messages) {
        RequestBuffer.request(() -> {
            if (channel.getModifiedPermissions(Enigma.getClient().getOurUser())
                    .contains(Permissions.MANAGE_MESSAGES))
                channel.bulkDelete(messages);
        });
    }

    public static void deleteMessage(IMessage message) {
        RequestBuffer.request(() -> {
            if (message.getAuthor().equals(Enigma.getClient().getOurUser())
                    || message.getChannel().getModifiedPermissions(Enigma.getClient().getOurUser())
                    .contains(Permissions.MANAGE_MESSAGES))
                message.delete();
        });
    }

    public static void deleteMessage(int delay, IMessage message) {
        Enigma.SCHEDULER.schedule(() -> deleteMessage(message), delay, TimeUnit.SECONDS);
    }

    public static IMessage sendMessage(IChannel channel, String content) {
        if (content.trim().isEmpty()) return null;
        return RequestBuffer.request(() -> {
            if (channel.getModifiedPermissions(Enigma.getClient().getOurUser())
                    .contains(Permissions.SEND_MESSAGES))
                return channel.sendMessage(content);
            return null;
        }).get();
    }

    public static IMessage sendMessage(IChannel channel, EmbedObject embed) {
        return RequestBuffer.request(() -> {
            if (channel.getModifiedPermissions(Enigma.getClient().getOurUser())
                    .contains(Permissions.SEND_MESSAGES))
                return channel.sendMessage(embed);
            return null;
        }).get();
    }

    public static IMessage sendMessage(IChannel channel, String content, EmbedObject embed) {
        if (content.trim().isEmpty()) return null;
        return RequestBuffer.request(() -> {
            if (channel.getModifiedPermissions(Enigma.getClient().getOurUser())
                    .contains(Permissions.SEND_MESSAGES))
                return channel.sendMessage(content, embed);
            return null;
        }).get();
    }
}
