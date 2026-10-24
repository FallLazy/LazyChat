package dev.falllazy.lazyChat.chatSystem;

import dev.falllazy.lazyChat.chatSystem.configs.ChatTemplate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class CManager {
    private final JavaPlugin plugin;
    private final Map<String, Chat> chats = new HashMap<>();
    private Chat defaultChat;
    private boolean experimentalJson;

    public CManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        chats.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection chatsSection = config.getConfigurationSection("chats");
        if (chatsSection == null) {
            plugin.getLogger().warning("No 'chats' section in config.yml");
            return;
        }

        experimentalJson = config.getBoolean("options.experiments.jsonForChats", false);

        for (String key : chatsSection.getKeys(false)) {
            ConfigurationSection chatConfig = chatsSection.getConfigurationSection(key);
            if (chatConfig == null) continue;

            List<String> prefixes = new ArrayList<>();
            Object prefixObj = chatConfig.get("prefix");
            if (prefixObj instanceof String single) {
                if (!single.isEmpty()) prefixes.add(single);
            } else if (prefixObj instanceof List) {
                prefixes.addAll((List<String>) prefixObj);
            }

            int radius = chatConfig.getInt("radius", -1);
            String perm = chatConfig.getString("perm", "");

            String templateName = chatConfig.getString("template");
            if (templateName == null || templateName.isEmpty()) {
                plugin.getLogger().warning("Chat '" + key + "' has no template defined, skipping");
                continue;
            }

            File templateFile = new File(plugin.getDataFolder(), "/templates/" + templateName);
            if (!templateFile.exists()) {
                plugin.getLogger().warning("Template file not found: " + templateFile.getPath());
                continue;
            }

            try {
                ChatTemplate template = ChatTemplate.load(templateFile, experimentalJson);

                Double templateCooldown = template.cooldown();
                double chatCooldown = (templateCooldown != null) ? templateCooldown : config.getDouble("options.chat-default-cooldown", 1.0);

                Chat chat = new Chat(key, prefixes, radius, perm, template, chatCooldown);
                chats.put(key, chat);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load template '" + templateName + "' for chat '" + key + "': " + e.getMessage());
            }
        }

        defaultChat = chats.get("local");
        if (defaultChat == null) {
            for (Chat chat : chats.values()) {
                if (chat.prefixes().isEmpty()) {
                    defaultChat = chat;
                    break;
                }
            }
        }
        if (defaultChat == null && !chats.isEmpty()) {
            defaultChat = chats.values().iterator().next();
        }

        plugin.getLogger().info("Loaded " + chats.size() + " chat(s). Default: " + (defaultChat != null ? defaultChat.name() : "none"));
    }

    public Chat getChatForMessage(String message) {
        if (message == null) return defaultChat;
        for (Chat chat : chats.values()) {
            for (String prefix : chat.prefixes()) {
                if (message.startsWith(prefix)) {
                    return chat;
                }
            }
        }
        return defaultChat;
    }

    public Chat getDefaultChat() { return defaultChat; }
    public Map<String, Chat> getChats() { return Collections.unmodifiableMap(chats); }
    public boolean isExperimentalJson() { return experimentalJson; }
}
