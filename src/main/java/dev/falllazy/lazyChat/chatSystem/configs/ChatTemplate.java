package dev.falllazy.lazyChat.chatSystem.configs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public record ChatTemplate(String prompt, BlacklistConfig blacklist, Double cooldown) {

    public static ChatTemplate load(File file, boolean experimentalJson) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".yml") || name.endsWith(".yaml")) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            String prompt = yaml.getString("config.prompt", "");
            Double cooldown = yaml.contains("config.cooldown") ? yaml.getDouble("config.cooldown") : null;
            BlacklistConfig blacklist = new BlacklistConfig();
            blacklist.setGlobal(yaml.getBoolean("blacklist.global", true));
            if (!blacklist.isGlobal()) {
                blacklist.setMinimessage(yaml.getBoolean("blacklist.config.minimessage", false));
                blacklist.setGradients(yaml.getBoolean("blacklist.config.gradients", false));
                blacklist.setTags(yaml.getStringList("blacklist.config.tags"));
            }
            return new ChatTemplate(prompt, blacklist, cooldown);
        } else if (name.endsWith(".json") && experimentalJson) {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(file)) {
                JsonObject root = gson.fromJson(reader, JsonObject.class);
                JsonObject config = root.getAsJsonObject("config");
                String prompt = config.get("prompt").getAsString();
                Double cooldown = config.has("cooldown") ? config.get("cooldown").getAsDouble() : null;
                JsonObject blacklistObj = root.getAsJsonObject("blacklist");
                BlacklistConfig blacklist = new BlacklistConfig();
                blacklist.setGlobal(blacklistObj.get("global").getAsBoolean());
                if (!blacklist.isGlobal()) {
                    JsonObject cfg = blacklistObj.getAsJsonObject("config");
                    blacklist.setMinimessage(cfg.get("minimessage").getAsBoolean());
                    blacklist.setGradients(cfg.get("gradients").getAsBoolean());
                    blacklist.setTags(gson.fromJson(cfg.get("tags"), List.class));
                }
                return new ChatTemplate(prompt, blacklist, cooldown);
            }
        } else {
            throw new IOException("Unsupported file format or JSON experiments disabled");
        }
    }
}