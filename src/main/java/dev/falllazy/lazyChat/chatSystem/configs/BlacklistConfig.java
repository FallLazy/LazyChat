package dev.falllazy.lazyChat.chatSystem.configs;

import java.util.List;

public class BlacklistConfig {
    private boolean global = true;
    private boolean minimessage = false;
    private boolean gradients = false;
    private List<String> tags = List.of();

    public boolean isGlobal() { return global; }
    public void setGlobal(boolean global) { this.global = global; }
    public boolean isMinimessage() { return minimessage; }
    public void setMinimessage(boolean minimessage) { this.minimessage = minimessage; }
    public boolean isGradients() { return gradients; }
    public void setGradients(boolean gradients) { this.gradients = gradients; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}