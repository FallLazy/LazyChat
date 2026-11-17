package dev.falllazy.lazyChat;

import dev.falllazy.lazyChat.api.tools.ChatBubbleTools;
import dev.falllazy.lazyChat.api.tools.FeatureChecker;
import dev.falllazy.lazyChat.autoMessages.AutoMessages;
import dev.falllazy.lazyChat.chatSystem.CManager;
import dev.falllazy.lazyChat.chatSystem.ChatListener;
import dev.falllazy.lazyChat.chatSystem.CooldownThings;
import dev.falllazy.lazyChat.language.LanguageManager;
import dev.falllazy.lazyChat.mute.MuteManager;
import dev.falllazy.lazyChat.commands.BCCommand;
import dev.falllazy.lazyChat.commands.LCCommandKt;
import dev.falllazy.lazyChat.commands.muteCommand.MuteCommand;
import dev.falllazy.lazyChat.commands.muteCommand.MuteCommandCompleter;
import dev.falllazy.lazyChat.listeners.FormatListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@SuppressWarnings(value = "UnstableApiUsage")
public final class LazyChat extends JavaPlugin {
    public String currentVersion = this.getPluginMeta().getVersion().toLowerCase();
    public MuteManager muteManager;
    private LanguageManager lang;
    private CManager chatManager;
    private CooldownThings chatCooldown;
    private CooldownThings broadcastCooldown;
    private AutoMessages autoMessages;
    private ChatBubbleTools chatBubbleTools;

    @Override
    public void onEnable() {
        if (!FeatureChecker.Bubble.isSupported()) {
            getLogger().severe("Chat bubbles cannot be used because it's not supported on your server!");
        }

        chatBubbleTools = new ChatBubbleTools(this);
        saveDefaultConfig();
        sendLog();
        saveFiles();
        //version
        new Checker(this).getLatestVersion(latest -> {
            if (currentVersion.equalsIgnoreCase(latest)) {
                getLogger().info("You're using actual version.");
            }
            else {
                getLogger().warning("Wait, you not updated a lot!");
                getLogger().warning("Current version: " + currentVersion);
                getLogger().warning("New version: " + latest);
                getLogger().warning("Download: https://modrinth.com/plugin/lazychat/versions/" + latest);
            }
        });
        convertConfig();
        //language
        lang = new LanguageManager(this);
        // Mute :P
        boolean yaml = getConfig().getBoolean("options.deprecations.yamlForMute", false); // just for support
        muteManager = new MuteManager(this, yaml);

        // Auto messages
        autoMessages = new AutoMessages(this);

        // commands init
        Objects.requireNonNull(getCommand("lazychat")).setExecutor(new LCCommandKt(this));
        Objects.requireNonNull(getCommand("lazychat")).setTabCompleter(new LCCommandKt(this));
        Objects.requireNonNull(getCommand("lbroadcast")).setExecutor(new BCCommand(this));
        Objects.requireNonNull(getCommand("l-mute")).setExecutor(new MuteCommand(this));
        Objects.requireNonNull(getCommand("l-mute")).setTabCompleter(new MuteCommandCompleter());

        // listeners
        getServer().getPluginManager().registerEvents(new FormatListener(), this);

        // cooldowns, hehe
        chatCooldown = new CooldownThings();
        broadcastCooldown = new CooldownThings();

        // And finally, CHAT!
        if (FeatureChecker.Bubble.isSupported()) getLogger().info("Tip: You can use Chat Bubbles!");
        chatManager = new CManager(this);
        getServer().getPluginManager().registerEvents(new ChatListener(this,chatManager), this);

        // and there bstats
        int pluginId = 35362;
        new Metrics(this, pluginId);
    }
    public LanguageManager getLang() {
        return lang;
    }
    public CManager getChatManager() {
        return chatManager;
    }
    public CooldownThings getChatCooldown() {
        return chatCooldown;
    }
    public CooldownThings getBroadcastCooldown() {
        return broadcastCooldown;
    }
    public AutoMessages getAutoMessages() {
        return autoMessages;
    }
    public ChatBubbleTools getChatBubbleTools() {
        return chatBubbleTools;
    }

    private void saveFiles() {
        saveResource("lang/English.yml", true);
        saveResource("lang/Russian.yml", true);
        saveResource("lang/Ukrainian.yml", true);
        saveResource("data/ban-words.yml", false);
        if (getConfig().getBoolean("options.save-internal-chats")) {
            saveResource("templates/admin.yml", false);
            saveResource("templates/business.json", false);
            saveResource("templates/global.yml", false);
            saveResource("templates/local.yml", false);
            saveResource("templates/whisper.yml", false);
        }
    }
    private void convertConfig() {
        if (!getConfig().contains("config-version") || getConfig().getInt("config-version") < 5) {
            getLogger().severe("Unknown config version or not supported. Deleting it and creating default");

            saveResource("config.yml", true);
            reloadConfig();
            return;
        }

        int currentVersion = getConfig().getInt("config-version");

        if (currentVersion < 7) {
            getLogger().warning("Config version is lower than 7. Converting it");

            boolean databaseForMutes = getConfig().getBoolean("options.experiments.databasesForMutes");
            boolean mentionsEnabled = getConfig().getBoolean("options.experiments.mentions");

            getConfig().set("config-version", 7);
            getConfig().set("options.deprecations.ymlForMute", !databaseForMutes);
            getConfig().set("options.experiments.databasesForMutes", null);
            getConfig().set("options.experiments.mentions.enabled", mentionsEnabled);

            getConfig().options().copyDefaults(true);

            saveConfig();
            getLogger().info("Converted!");
        }
        else if (currentVersion == 7) {
            getLogger().info("Config version is actual!");
        }
    }

    private void sendLog() {
        String asciiArt = """
            ##         #####    #########  ##      ##    ######   ##      ##   #####    #########
            ##        ##   ##          ##  ##      ##   ##    ##  ##      ##  ##   ##       ##
            ##       ##     ##        ##    ##    ##    ##        ##      ## ##     ##      ##
            ##       #########       ##      ##  ##     ##        ########## #########      ##
            ##       ##     ##      ##        ####      ##        ##      ## ##     ##      ##
            ##       ##     ##     ##          ##       ##    ##  ##      ## ##     ##      ##
            ##       ##     ##    ##           ##        #    #   ##      ## ##     ##      ##
            ######## ##     ##    #########    ##        ######   ##      ## ##     ##      ##
            """;
        for (String line : asciiArt.split("\n")) {
            getServer().getConsoleSender().sendMessage(line);
        }
    }

    @Override
    public void onDisable() {
        if (muteManager != null) {
            muteManager.close();
        }
        chatBubbleTools.shutdown();
    }
}