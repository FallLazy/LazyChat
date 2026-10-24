package dev.falllazy.lazyChat.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class FormatListener implements Listener {
    private final MiniMessage mm = MiniMessage.miniMessage();

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("l-chat.advance_format")) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            Component lineComponent = event.line(i);

            if (lineComponent == null) continue;

            String rawText = PlainTextComponentSerializer.plainText().serialize(lineComponent);

            if (rawText.isEmpty()) continue;

            if (rawText.contains("<newline>") && !player.hasPermission("l-chat.full-format-use")) {
                continue;
            }

            Component formattedLine = mm.deserialize(rawText);
            event.line(i, formattedLine);
        }
    }

}