package dev.falllazy.lazyChat;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class Checker {
    private final JavaPlugin plugin;
    private static final String MODRINTH_SLUG = "lazychat";

    public Checker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void getLatestVersion(Consumer<String> consumer) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String baseUrl = "https://api.modrinth.com/v2/project/" + MODRINTH_SLUG + "/version";
                String query = "?loaders=" + URLEncoder.encode("[\"paper\"]", StandardCharsets.UTF_8.name());
                URL url = new URL(baseUrl + query);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "LazyCat0/LazyChat/" + plugin.getPluginMeta().getVersion() + " (LazyCat.de@proton.me)");

                if (connection.getResponseCode() == 200) {
                    try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                        JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
                        if (!jsonArray.isEmpty()) {
                            String latestVersion = jsonArray.get(0).getAsJsonObject().get("version_number").getAsString();
                            consumer.accept(latestVersion);
                        } else {
                            plugin.getLogger().warning("No versions found for project " + MODRINTH_SLUG);
                        }
                    }
                } else {
                    plugin.getLogger().warning("Cannot check for updates. HTTP Code: " + connection.getResponseCode());
                }
                connection.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("Cannot check for updates: " + e.getMessage());
            }
        });
    }
}