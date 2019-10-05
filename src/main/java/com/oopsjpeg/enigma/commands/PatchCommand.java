package com.oopsjpeg.enigma.commands;

import com.oopsjpeg.enigma.util.Command;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;

public class PatchCommand implements Command {
    @Override
    public void execute(Message message, String alias, String[] args) {
        message.getChannel().block().createEmbed(e -> {
            e.setTitle("Items");
            e.addField("Potion", "Heal: ~~160~~ **120**", true);
            e.addField("Steel Mallet", "Max Health: ~~25~~ **30**", true);
            e.addField("Dawn Hammer", "Max Health: ~~75~~ **100**", true);
            e.addField("Crimson Buckler", "Brawn: ~~3%~~ **4%**", true);
            e.addField("Black Halberd", "Damage: ~~6~~ **8**", true);
            e.addField("Viktor's Scythe", "Damage: ~~16~~ **20**", true);
        }).block();
        message.getChannel().block().createEmbed(e -> {
            e.setTitle("Units");
            e.addField("Berserker", "Can no longer rage at 0 stacks.", true);
            e.addField("Warrior", "**Bash** now breaks resist and defensive stance.", true);
            e.addField("Assassin", "Can no longer slash twice in a turn after reset.", true);
        }).block();
    }

    @Override
    public String getName() {
        return "patch";
    }

    @Override
    public PermissionSet getPermissions() {
        return PermissionSet.of(Permission.MANAGE_GUILD);
    }
}
