package dev.falllazy.lazyChat.api.events;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MessageSentEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Player player;
    private boolean isMuted;
    private boolean cancelled;
    private Component messageContent;

    /**
     * Event, that says: "Hey, player just sent minimessage-message!"
     * @param player {@link Player} • Author of message
     * @param isMuted boolean • Did player muted? (can be used with other plugins with mutes/if you get problems with other plugins)
     * @param messageContent {@link Component} • Content of message.
     */
    public MessageSentEvent(Player player, boolean isMuted, Component messageContent) {
        this.player = player;
        this.isMuted = isMuted;
        this.messageContent = messageContent;
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

    /**
     * Returns player that sent message
     * @return {@link Player}
     */
    public Player getPlayer() {
        return player;
    }
    /**
     * Returns "Is player muted?"
     * @return boolean
     */
    public boolean isMuted() {
        return isMuted;
    }
    /**
     * Returns message content
     * @return {@link Component}
     */
    public Component getMessageContent() {
        return messageContent;
    }
    /**
     * Returns message content but as String
     * @return {@link String}
     */
    public String getMessageStringContent() {
        return messageContent.toString();
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public void setMessageContent(Component messageContent) {
        this.messageContent = messageContent;
    }
}
