package dev.falllazy.lazyChat.autoMessages;

import dev.falllazy.lazyChat.LazyChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoMessages {
    private final LazyChat plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private File messagesFile;
    private FileConfiguration messages;

    private final Map<String, AutoMessage> autoMessageMap = new HashMap<>();
    private final List<BukkitTask> runningTasks = new ArrayList<>();

    public AutoMessages(LazyChat plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        stopAll();

        messagesFile = new File(plugin.getDataFolder(), "data/automessages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("data/automessages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        autoMessageMap.clear();

        for (String key : messages.getKeys(false)) {
            ConfigurationSection section = messages.getConfigurationSection(key);
            if (section == null) continue;

            String message = section.getString("message", "");
            double time = section.getDouble("time", 0);
            boolean enabled = section.getBoolean("enabled", false);

            if (message.isEmpty() || time <= 0) {
                plugin.getLogger().warning("AutoMessage '" + key + "' skipped (empty message or time <= 0)");
                continue;
            }
            autoMessageMap.put(key, new AutoMessage(message, time, enabled));
        }

        startAll();
        plugin.getLogger().info("Loaded " + autoMessageMap.size() + " auto-message(s).");
    }

    private void startAll() {
        for (Map.Entry<String, AutoMessage> entry : autoMessageMap.entrySet()) {
            AutoMessage am = entry.getValue();
            if (!am.enabled()) continue;

            long ticks = Math.round(am.inSeconds() * 20.0);
            if (ticks <= 0) continue;

            Component parsed = mm.deserialize(am.message());

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(parsed);
                }
            }, ticks, ticks);

            runningTasks.add(task);
        }
    }

    public void stopAll() {
        for (BukkitTask task : runningTasks) {
            if (!task.isCancelled()) task.cancel();
        }
        runningTasks.clear();
    }

    public Map<String, AutoMessage> getAutoMessageMap() {
        return Map.copyOf(autoMessageMap);
    }
}