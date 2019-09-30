package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.util.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.EnumSet;

public class PatchCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        MessageChannel channel = message.getChannel();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Items");
        builder.addField("Ring", "Ability Power: ~~8~~ **10**", true);
        builder.addField("Hatchet", "Cost: ~~375~~ **425**", true);
        builder.addField("Staff", "Cost: ~~450~~ **425**", true);
        builder.addField("Bloodlust Blade", "Life Steal: ~~10%~~ **15%**", true);
        builder.addField("Steel Mallet", "Cost: ~~575~~ **625**\nDamage: ~~6~~ **8**", true);
        builder.addField("Shadow Gauntlet", "Build: ~~Staff~~ **Ring**", true);
        builder.addField("Dawn Hammer", "Cost: ~~1100~~ **1175**\nBuild: ~~Knife~~ **Gemheart**\nMax Health: ~~40~~ **75**", true);
        builder.addField("Soulstealer", "Cost: ~~1250~~ **1325**\nDamage: ~~25~~ **30**\nLife Steal: ~~25%~~ **30%**", true);
        builder.addField("Iron Scimitar", "Cost: ~~1225~~ **1375**", true);
        channel.sendMessage(builder.build()).complete();

        builder.clear();
        builder.setTitle("Units");
        builder.addField("Assassin", "Slash now scales with AP.\nDamage to potency %: ~~10%-30%~~ **20%**", true);
        builder.addField("Berserker", "Bonus damage now scales with AP.", true);
        builder.addField("Thief", "Gold steal now scales with AP.", true);
        channel.sendMessage(builder.build()).complete();
    }

    @Override
    public String getName() {
        return "patch";
    }

    @Override
    public EnumSet<Permission> getPermissions() {
        return EnumSet.of(Permission.MANAGE_SERVER);
    }
}
