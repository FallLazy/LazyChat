package dev.falllazy.lazyChat.chatSystem.configs;

@SuppressWarnings("unused")
public class ChatBubble {
    private boolean enabled = false;
    private double showTime = 2.2;
    private String prompt;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getShowTime() {
        return showTime;
    }

    public void setShowTime(double showTime) {
        this.showTime = showTime;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
