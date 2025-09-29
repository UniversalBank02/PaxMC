package me.universalbank.commands;

import me.universalbank.AutoBroadcast;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaxCommands implements CommandExecutor {

    private final AutoBroadcast autoBroadcast;
    private final JavaPlugin plugin;
    private Map<String, CommandEntry> commands;

    public PaxCommands(JavaPlugin plugin, AutoBroadcast autoBroadcast) {
        this.plugin = plugin;
        this.autoBroadcast = autoBroadcast;
        loadCommands();
    }

    public void loadCommands() {
        File file = new File(plugin.getDataFolder(), "commands.yml");
        if (!file.exists()) plugin.saveResource("commands.yml", false);

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        commands = new HashMap<>();

        for (String key : cfg.getKeys(false)) {
            List<String> messages = cfg.getStringList(key + ".messages");
            List<String> hover = cfg.getStringList(key + ".hover");
            String click = cfg.getString(key + ".click", "");
            commands.put(key.toLowerCase(), new CommandEntry(messages, click, hover));
        }
    }

    public Map<String, CommandEntry> getCommands() {
        return commands;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("paxmc.admin")) {
                autoBroadcast.reload();
                loadCommands();
                sender.sendMessage(ChatColor.GREEN + "PaxMC configuration reloaded!");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
            }
            return true;
        }

        String cmdName = command.getName().toLowerCase();

        if (commands.containsKey(cmdName)) {
            CommandEntry entry = commands.get(cmdName);
            for (String line : entry.messages) {
                TextComponent text = new TextComponent(ChatColor.translateAlternateColorCodes('&', line));

                // Clickable
                if (!entry.click.isEmpty()) {
                    if (entry.click.startsWith("/")) {
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, entry.click));
                    } else {
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, entry.click));
                    }
                }

                // Hover
                if (!entry.hover.isEmpty()) {
                    String hoverText = String.join("\n", entry.hover);
                    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText)).create()));
                }

                if (sender instanceof Player p) {
                    p.spigot().sendMessage(text);
                } else {
                    sender.sendMessage(ChatColor.stripColor(line));
                }
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown command.");
        return true;
    }

    private static class CommandEntry {
        final List<String> messages;
        final String click;
        final List<String> hover;

        public CommandEntry(List<String> messages, String click, List<String> hover) {
            this.messages = messages;
            this.click = click;
            this.hover = hover;
        }
    }
}
