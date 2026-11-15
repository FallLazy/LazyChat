package dev.falllazy.lazyChat.api.events;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MentionEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;
    private Player messageAuthor;
    private Player mentioned;
    private final Component messageContent;

    /**
     * Event, that says: "Hey, player just sent minimessage-message!"
     * @param messageAuthor {@link Player} • Author of message
     * @param mentioned {@link Player} • Who was mentioned
     * @param messageContent {@link Component} • Content of message.
     */
    public MentionEvent(Player messageAuthor, Player mentioned, Component messageContent) {
        this.messageAuthor = messageAuthor;
        this.mentioned = mentioned;
        this.messageContent = messageContent;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public Player getMessageAuthor() {
        return messageAuthor;
    }

    public void setMessageAuthor(Player messageAuthor) {
        this.messageAuthor = messageAuthor;
    }

    public Player getMentioned() {
        return mentioned;
    }

    public void setMentioned(Player mentioned) {
        this.mentioned = mentioned;
    }

    public Component getMessageContent() {
        return messageContent;
    }
}
