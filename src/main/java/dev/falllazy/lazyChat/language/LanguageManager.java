package dev.falllazy.lazyChat.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
public class LanguageManager {
    private final Map<String, FileConfiguration> languages = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final FileConfiguration lang;
    public LanguageManager(JavaPlugin plugin) {
        String defaultLang = plugin.getConfig().getString("options.language", "English");
        loadLanguages(plugin);
        this.lang = languages.get(defaultLang);
    }
    public void loadLanguages(JavaPlugin plugin) {
        File dir = new File(plugin.getDataFolder(), "lang");
        plugin.saveResource("lang/Russian.yml", true);
        plugin.saveResource("lang/English.yml", true);
        plugin.saveResource("lang/Ukrainian.yml", true);
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (!file.getName().endsWith(".yml")) continue;
            String lang = file.getName().replace(".yml", "");
            languages.put(lang, YamlConfiguration.loadConfiguration(file));
        }
    }

    public String getRaw(String path) {
        @NotNull String raw = Objects.requireNonNull(lang.getString(path));
        return raw;
    }
    public Component getFormated(@NotNull String path) {
        return mm.deserialize(getRaw(path));
    }
    public Component getFormated(@NotNull String path, final TagResolver... resolvers) {
        return mm.deserialize(getRaw(path), TagResolver.resolver(resolvers));
    }
}
// by LazyCato0o