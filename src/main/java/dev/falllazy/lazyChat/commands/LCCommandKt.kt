package dev.falllazy.lazyChat.commands

import dev.falllazy.lazyChat.LazyChat
import dev.falllazy.lazyChat.language.LanguageManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class LCCommandKt(private val plugin: LazyChat) : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) {
            return true
        }

        val act: String = args[0].lowercase()
        val lang: LanguageManager = plugin.lang
        when (act){
            "reload" -> {
                if (!sender.hasPermission("l-chat.reload")) {
                    sender.sendMessage(lang.getFormated("lazychat.messages.cannot_use"))
                    return true
                }
                sender.sendMessage(lang.getFormated("lazychat.messages.reload.reloading"))
                try {
                    plugin.reloadConfig()
                    lang.loadLanguages(plugin)
                    plugin.chatManager.reload()
                    plugin.muteManager.reload()
                    plugin.autoMessages.load()

                    sender.sendMessage(lang.getFormated("lazychat.messages.reload.success"))
                    plugin.logger.info("Successfully reloaded plugin")
                } catch (e: Exception ) {
                    sender.sendMessage(lang.getFormated("lazychat.messages.reload.error",
                        Placeholder.parsed("e_message", e.message ?: "Unknown error")
                    ))
                    plugin.logger.severe("While plugin reloads, plugin was meets error - " + e.message)
                }
            }
            "info" -> sender.sendMessage(lang.getFormated("lazychat.messages.info"))
            "clear" -> {
                for (player in Bukkit.getOnlinePlayers()) {
                    for (i in 0..199) {
                        player.sendMessage(Component.empty())
                    }
                }
                sender.sendMessage(lang.getFormated("lazychat.messages.cleared"))
            }
            else -> {
                sender.sendMessage(lang.getFormated("lazychat.messages.usage"))
            }
        }

        return true
    }

    override fun onTabComplete(
        s: CommandSender,
        c: Command,
        l: String,
        a: Array<out String>
    ): List<String?> {
        if (a.size == 1) {
            return mutableListOf<String?>("reload", "info", "clear")
        }
        return mutableListOf()
    }
}