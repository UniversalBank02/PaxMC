package me.universalbank;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

public class AutoBroadcast {

    private final JavaPlugin plugin;
    private final List<String> keys;
    private final Map<String, BroadcastEntry> entries;
    private BukkitTask task;
    private int index = 0;
    private int interval;
    private boolean random;

    public AutoBroadcast(JavaPlugin plugin) {
        this.plugin = plugin;
        keys = new ArrayList<>();
        entries = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        File file = new File(plugin.getDataFolder(), "autobroadcast.yml");
        if (!file.exists()) plugin.saveResource("autobroadcast.yml", false);

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        interval = cfg.getInt("time-between-messages", 900);
        random = cfg.getBoolean("random", false);

        ConfigurationSection broadcasts = cfg.getConfigurationSection("broadcasts");
        keys.clear();
        entries.clear();

        if (broadcasts != null) {
            for (String key : broadcasts.getKeys(false)) {
                ConfigurationSection sec = broadcasts.getConfigurationSection(key);
                if (sec == null) continue;
                BroadcastEntry entry = new BroadcastEntry(sec);
                entries.put(key, entry);
                keys.add(key);
            }
        }
    }

    public void reload() {
        stop();
        loadConfig();
        start();
    }

    public void start() {
        if (task != null) return;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::sendNext,
                interval * 20L, interval * 20L);
    }

    public void stop() {
        if (task != null) task.cancel();
        task = null;
    }

    private void sendNext() {
        if (keys.isEmpty()) return;
        String key = random ? keys.get(new Random().nextInt(keys.size()))
                : keys.get(index++ % keys.size());
        BroadcastEntry entry = entries.get(key);
        if (entry == null) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (entry.exempted.contains(p.getName().toLowerCase())) continue;
            entry.sendTo(p);
        }
    }

    private static class BroadcastEntry {
        final List<String> messages;
        final String click;
        final List<String> hover;
        final Sound sound;
        final Set<String> exempted;

        BroadcastEntry(ConfigurationSection sec) {
            messages = sec.getStringList("messages");
            click = sec.getString("click", "");
            hover = sec.getStringList("hover");
            sound = parseSound(sec.getString("sound"));
            exempted = new HashSet<>();
            for (String s : sec.getStringList("exempted-players")) {
                exempted.add(s.toLowerCase());
            }
        }

        void sendTo(Player player) {
            for (String msg : messages) {
                TextComponent text = new TextComponent(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));

                // Clickable
                if (!click.isEmpty()) {
                    if (click.startsWith("/")) {
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, click));
                    } else {
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, click));
                    }
                }

                // Hover
                if (!hover.isEmpty()) {
                    String hoverText = String.join("\n", hover);
                    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(org.bukkit.ChatColor.translateAlternateColorCodes('&', hoverText)).create()));
                }

                player.spigot().sendMessage(text);
            }

            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1f, 1f);
            }
        }

        private static Sound parseSound(String s) {
            try {
                return s == null ? null : Sound.valueOf(s.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                return null;
            }
        }
    }
}
