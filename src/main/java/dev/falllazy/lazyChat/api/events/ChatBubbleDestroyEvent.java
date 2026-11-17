package dev.falllazy.lazyChat.api.events;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ChatBubbleDestroyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;
    private Component messageContent;
    private Location location;
    private TextDisplay bubble;

    public ChatBubbleDestroyEvent(boolean cancelled, Component messageContent, Location location, TextDisplay bubble) {
        this.cancelled = cancelled;
        this.messageContent = messageContent;
        this.location = location;
        this.bubble = bubble;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public Component getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(Component messageContent) {
        this.messageContent = messageContent;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public TextDisplay getBubble() {
        return bubble;
    }

    public void setBubble(TextDisplay bubble) {
        this.bubble = bubble;
    }
}
