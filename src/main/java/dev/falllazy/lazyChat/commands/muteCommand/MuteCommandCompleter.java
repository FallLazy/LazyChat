package dev.falllazy.lazyChat.commands.muteCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MuteCommandCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return List.of("mute", "unmute");
        }
        if (args.length == 2) {
            return List.of("player-nickname-that-you-want-mute");
        }
        if (args.length == 4) {
            return List.of("minute", "hour", "day");
        }
        return List.of();
    }
}
// by LazyCato0o