package com.oopsjpeg.enigma.listener;

import com.oopsjpeg.enigma.Enigma;
import com.oopsjpeg.enigma.game.object.Unit;
import com.oopsjpeg.enigma.util.Listener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;

public class ComponentListener implements Listener
{
    private final Enigma instance;

    public ComponentListener(Enigma instance)
    {
        this.instance = instance;
    }

    private void onSelectMenuInteraction(SelectMenuInteractionEvent event)
    {
        GatewayDiscordClient client = event.getClient();

        // Unit viewer
        if (event.getCustomId().equals("unit_viewer"))
        {
            MessageChannel channel = event.getMessage().get().getChannel().block();
            Unit unit = Unit.valueOf(event.getValues().get(0));

            // Create a temp button to view stats
            Button statsBtn = Button.primary("unit_viewer-stats;" + unit.name(), "View Stats");

            event.reply(InteractionApplicationCommandCallbackSpec.builder()
                    .ephemeral(true)
                    .addEmbed(unit.format())
                    .addComponent(ActionRow.of(statsBtn))
                    .build()).subscribe();
        }
    }

    public void onButtonInteractionEvent(ButtonInteractionEvent event)
    {
        String[] idChunks = event.getCustomId().split(";");
        String id = idChunks[0];

        // Unit viewer for stats
        if (id.equals("unit_viewer-stats"))
        {
            Unit unit = Unit.valueOf(idChunks[1]);

            event.reply(InteractionApplicationCommandCallbackSpec.builder()
                    .ephemeral(true)
                    .addEmbed(unit.formatStats())
                    .build()).subscribe();
        }
    }

    @Override
    public void register(GatewayDiscordClient client)
    {
        client.on(SelectMenuInteractionEvent.class).subscribe(this::onSelectMenuInteraction);
        client.on(ButtonInteractionEvent.class).subscribe(this::onButtonInteractionEvent);
    }

    @Override
    public Enigma getInstance()
    {
        return instance;
    }
}
