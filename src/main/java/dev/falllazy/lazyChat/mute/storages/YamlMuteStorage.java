package dev.falllazy.lazyChat.mute.storages;

import dev.falllazy.lazyChat.LazyChat;
import dev.falllazy.lazyChat.language.LanguageManager;
import dev.falllazy.lazyChat.mute.MuteInfo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YamlMuteStorage implements MuteStorage {
    private final File file;
    private final FileConfiguration config;
    private final LazyChat plugin;
    private final LanguageManager lang;

    public YamlMuteStorage(LazyChat plugin) {
        this.plugin = plugin;
        plugin.getLogger().warning("YML mute storage is deprecated!");
        this.file = new File(plugin.getDataFolder(), "data/muted_players.yml");
        if (!file.exists()) {
            plugin.saveResource("data/muted_players.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        this.lang = plugin.getLang();
    }

    @Override
    public Map<UUID, MuteInfo> loadMutes() {
        Map<UUID, MuteInfo> result = new HashMap<>();
        if (config.getConfigurationSection("mutes") == null) return result;

        for (String key : config.getConfigurationSection("mutes").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String path = "mutes." + key;
            if (config.isLong(path)) {
                long expiry = config.getLong(path);
                result.put(uuid, new MuteInfo(expiry, ""));
            } else {
                long expiry = config.getLong(path + ".expiry");
                String reason = config.getString(path + ".reason", "");
                result.put(uuid, new MuteInfo(expiry, reason));
            }
        }
        return result;
    }

    @Override
    public void saveMute(UUID uuid, MuteInfo info) {
        String path = "mutes." + uuid.toString();
        config.set(path + ".expiry", info.expiry());
        config.set(path + ".reason", info.reason());
        save();
    }

    @Override
    public void removeMute(UUID uuid) {
        config.set("mutes." + uuid.toString(), null);
        save();
    }

    @Override
    public boolean isMuted(UUID uuid) {
        MuteInfo info = getMuteInfo(uuid);
        return info != null && System.currentTimeMillis() <= info.expiry();
    }

    @Override
    public MuteInfo getMuteInfo(UUID uuid) {
        String path = "mutes." + uuid.toString();
        if (!config.contains(path)) return null;
        if (config.isLong(path)) {
            long expiry = config.getLong(path);
            return new MuteInfo(expiry, "");
        }
        long expiry = config.getLong(path + ".expiry");
        String reason = config.getString(path + ".reason", "");
        return new MuteInfo(expiry, reason);
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save mutes: " + e.getMessage());
        }
    }

    @Override
    public void close() {
    }
}