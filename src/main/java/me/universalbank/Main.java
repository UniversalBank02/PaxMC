package me.universalbank;

import me.universalbank.commands.PaxCommands;
import me.universalbank.listeners.CustomCrafts;
import me.universalbank.listeners.FortuneDisabler;
import me.universalbank.listeners.SmartSpawnerListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private AutoBroadcast autoBroadcast;
    private PaxCommands commandHandler;

    @Override
    public void onEnable() {
        getLogger().info("PaxMC plugin loaded.");

        saveResource("autobroadcast.yml", false);
        saveResource("commands.yml", false);

        autoBroadcast = new AutoBroadcast(this);
        autoBroadcast.start();
        getLogger().info("AutoBroadcast started.");
        new CustomCrafts(this).registerRecipes();
        commandHandler = new PaxCommands(this, autoBroadcast);
        if (getCommand("pax") != null) {
            getCommand("pax").setExecutor(commandHandler);
        }

        commandHandler.getCommands().keySet().forEach(cmd -> {
            if (!"pax".equalsIgnoreCase(cmd) && getCommand(cmd) != null) {
                getCommand(cmd).setExecutor(commandHandler);
            }
        });
        getServer().getPluginManager().registerEvents(new SmartSpawnerListener(), this);
        getServer().getPluginManager().registerEvents(new FortuneDisabler(), this);
        FortuneDisabler.purgeExistingFortune();
        getLogger().info("Fortune enchantment disabled and purged from loaded players.");
    }

    @Override
    public void onDisable() {
        getLogger().info("PaxMC plugin disabled.");
        if (autoBroadcast != null) autoBroadcast.stop();
    }
}
