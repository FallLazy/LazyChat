package dev.falllazy.lazyChat.chatSystem;

import dev.falllazy.lazyChat.LazyChat;
import dev.falllazy.lazyChat.chatSystem.configs.BlacklistConfig;
import dev.falllazy.lazyChat.language.LanguageManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatListener implements Listener {
    private final JavaPlugin plugin;
    private final LazyChat lazyChat;
    private final CManager chatManager;
    private final LanguageManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private LuckPerms luckPerms = null;

    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([a-zA-Z0-9_\\-]+)(:[^>]*)?>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<(gradient|rainbow)(:[^>]*)?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_]{1,16})");

    private File banWordsFile;
    private volatile CachedBanWords cachedBanWords;

    private record CachedBanWords(long mtime, Pattern pattern, List<String> words) {}

    public ChatListener(LazyChat plugin, CManager chatManager) {
        this.plugin = plugin;
        this.lazyChat = plugin;
        this.chatManager = chatManager;
        this.lang = plugin.getLang();
        if (plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            this.luckPerms = LuckPermsProvider.get();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (lazyChat.muteManager.isMuted(player.getUniqueId())) {
            player.sendMessage(lang.getFormated("mute.messages.you_muted"));
            event.setCancelled(true);
            return;
        }

        MiniMessage mm = MiniMessage.miniMessage();

        Component originalMessage = event.message();
        String plainText = PlainTextComponentSerializer.plainText().serialize(originalMessage);

        Chat chat = chatManager.getChatForMessage(plainText);
        if (chat == null) {
            chat = chatManager.getDefaultChat();
        }
        if (chat == null) {
            return;
        }

        if (!chat.canUse(player)) {
            player.sendMessage(lang.getFormated("other.not-enough-rights"));
            event.setCancelled(true);
            return;
        }

        String messageContent = plainText;
        for (String prefix : chat.prefixes()) {
            if (plainText.startsWith(prefix)) {
                messageContent = plainText.substring(prefix.length()).trim();
                break;
            }
        }
        if (messageContent.isEmpty()) {
            player.sendMessage(lang.getFormated("other.CSEM"));
            event.setCancelled(true);
            return;
        }

        BlacklistConfig blacklist = chat.template().blacklist();
        if (blacklist.isGlobal()) {
            blacklist = getGlobalBlacklist();
        }
        if (!isMessageAllowed(player, messageContent, blacklist)) {
            player.sendMessage(lang.getFormated("other.CUNT_cause_DNHP"));
            event.setCancelled(true);
            return;
        }

        messageContent = banWordFilter(player, messageContent, event);
        if (event.isCancelled()) {
            return;
        }

        Set<Player> mentioned = new HashSet<>();
        messageContent = applyMentions(messageContent, mentioned);

        String prompt = chat.template().prompt();
        Component formatted = mm.deserialize(prompt,
                Placeholder.parsed("m", messageContent),
                Placeholder.parsed("nickname", player.getName()),
                Placeholder.parsed("prefix", getPrefix(player)),
                Placeholder.parsed("suffix", getSuffix(player)),
                Placeholder.component("item", itemTag(player))
        );

        double cooldownSec = chat.cooldown();
        if (cooldownSec > 0) {
            if (!lazyChat.getChatCooldown().checkAndUpdate(player.getUniqueId(), cooldownSec)) {
                double remaining = lazyChat.getChatCooldown().getRemaining(player.getUniqueId(), cooldownSec);
                player.sendMessage(lang.getFormated("other.cooldown",
                        Placeholder.parsed("seconds", String.format("%.1f", remaining))
                ));
                event.setCancelled(true);
                return;
            }
        }

        int radius = chat.radius();
        Set<UUID> recipientIds = new HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            boolean inRange = radius == -1 || (p.getWorld().equals(player.getWorld())
                    && p.getLocation().distance(player.getLocation()) <= radius);
            if (inRange) {
                p.sendMessage(formatted);
                recipientIds.add(p.getUniqueId());
            }
        }

        if (plugin.getConfig().getBoolean("options.experiments.mentions")) {
            if (!mentioned.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (Player target : mentioned) {
                        if (target.getUniqueId().equals(player.getUniqueId())) continue;
                        if (!target.isOnline()) continue;
                        if (!recipientIds.contains(target.getUniqueId())) continue;
                        target.sendActionBar(lang.getFormated("other.you-mentioned"));
                        Sound sound = Sound.sound(Key.key("block.bell.hit"), Sound.Source.UI, 1f, 2f);
                        target.playSound(sound);
                    }
                });
            }
        }

        if (plugin.getConfig().getBoolean("options.enable-logging", true)) {
            plugin.getServer().getConsoleSender().sendMessage(formatted);
        }

        event.setCancelled(true);
    }

    private Component itemTag(Player player) {
        ItemStack itemStack = player.getActiveItem();

        if (itemStack.getType().isAir()) {
            return Component.empty();
        }

        ItemMeta meta = itemStack.getItemMeta();

        Component name = meta.hasItemName() ? meta.itemName() : Component.text(itemStack.getType().name());
        List<Component> lore = meta.hasLore() ? meta.lore() : List.of();

        Component hoverContent = Component.join(JoinConfiguration.newlines(), lore);

        return Component.text("[")
                .append(name)
                .append(Component.text("]"))
                .color(NamedTextColor.GRAY)
                .hoverEvent(HoverEvent.showText(hoverContent));
    }

    private String applyMentions(String text, Set<Player> mentionedOut) {
        Matcher matcher = MENTION_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            if (plugin.getConfig().getBoolean("options.experiments.mentions")) {
                String name = matcher.group(1);
                Player target = Bukkit.getPlayerExact(name);
                if (target != null && target.isOnline()) {
                    mentionedOut.add(target);
                    matcher.appendReplacement(sb, Matcher.quoteReplacement("<yellow>@" + name + "</yellow>"));
                } else {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private BlacklistConfig getGlobalBlacklist() {
        BlacklistConfig global = new BlacklistConfig();
        global.setGlobal(true);
        global.setMinimessage(plugin.getConfig().getBoolean("blacklist.chat.minimessage", false));
        global.setGradients(plugin.getConfig().getBoolean("blacklist.chat.gradients", false));
        global.setTags(plugin.getConfig().getStringList("blacklist.chat.tags"));
        return global;
    }

    private boolean isMessageAllowed(Player player, String message, BlacklistConfig blacklist) {
        if (blacklist.isMinimessage() && hasAnyTag(message)) {
            return false;
        }
        if (blacklist.isGradients() && hasGradientTag(message)) {
            return false;
        }
        if (!player.hasPermission("l-chat.advance_format")) {
            for (String tag : blacklist.getTags()) {
                if (hasSpecificTag(message, tag)) {
                    return false;
                }
            }
        }
        return true;
    }



    private CachedBanWords getBanWords() {
        if (banWordsFile == null) {
            banWordsFile = new File(plugin.getDataFolder(), "data/ban-words.yml");
        }
        long mtime = banWordsFile.lastModified();
        CachedBanWords cache = this.cachedBanWords;
        if (cache != null && cache.mtime() == mtime) {
            return cache;
        }
        cache = loadBanWords(banWordsFile, mtime);
        this.cachedBanWords = cache;
        return cache;
    }

    private CachedBanWords loadBanWords(File file, long mtime) {
        if (!file.exists()) {
            return new CachedBanWords(mtime, null, List.of());
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> words = config.getStringList("words").stream()
                .filter(w -> w != null && !w.isEmpty())
                .distinct()
                .toList();

        if (words.isEmpty()) {
            return new CachedBanWords(mtime, null, words);
        }

        String combined = words.stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        Pattern pattern = Pattern.compile(combined, Pattern.CASE_INSENSITIVE);
        return new CachedBanWords(mtime, pattern, words);
    }

    private String banWordFilter(Player player, String message, AsyncChatEvent event) {
        CachedBanWords banWords = getBanWords();
        if (banWords.pattern() == null) {
            return message;
        }

        boolean sendMessage = plugin.getConfig().getBoolean("ban-words.send-message");
        String type = plugin.getConfig().getString("ban-words.type", "replacement")
                .toLowerCase(Locale.ROOT);

        return switch (type) {
            case "replacement" -> {
                String replacement = Matcher.quoteReplacement(lang.getRaw("other.ban-word-replacement"));
                yield banWords.pattern().matcher(message).replaceAll(replacement);
            }
            case "cancel" -> {
                if (banWords.pattern().matcher(message).find()) {
                    event.setCancelled(true);
                    if (sendMessage) player.sendMessage(lang.getFormated("other.banned-word"));
                }
                yield message;
            }
            case "nothing" -> {
                if (banWords.pattern().matcher(message).find() && sendMessage) {
                    player.sendMessage(lang.getFormated("other.banned-word"));
                }
                yield message;
            }
            default -> throw new RuntimeException("Unknown type of ban-word filter: " + type);
        };
    }




    private boolean hasAnyTag(String message) {
        return TAG_PATTERN.matcher(message).find();
    }

    private boolean hasGradientTag(String message) {
        return GRADIENT_PATTERN.matcher(message).find();
    }

    private boolean hasSpecificTag(String message, String tagName) {
        Pattern pattern = Pattern.compile("<(/?)(?i:" + Pattern.quote(tagName) + ")(:[^>]*)?>");
        return pattern.matcher(message).find();
    }



    private String getPrefix(Player player) {
        if (luckPerms == null) return "";
        PlayerAdapter<Player> adapter = luckPerms.getPlayerAdapter(Player.class);
        CachedMetaData meta = adapter.getMetaData(player);
        String prefix = meta.getPrefix();
        return prefix != null ? prefix : "";
    }

    private String getSuffix(Player player) {
        if (luckPerms == null) return "";
        PlayerAdapter<Player> adapter = luckPerms.getPlayerAdapter(Player.class);
        CachedMetaData meta = adapter.getMetaData(player);
        String suffix = meta.getSuffix();
        return suffix != null ? suffix : "";
    }

}