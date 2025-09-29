package me.universalbank.listeners;

import github.nighter.smartspawner.api.SmartSpawnerAPI;
import github.nighter.smartspawner.api.SmartSpawnerProvider;
import github.nighter.smartspawner.api.events.SpawnerEggChangeEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class SmartSpawnerListener implements Listener {

    private final SmartSpawnerAPI smartSpawner = SmartSpawnerProvider.getAPI();

    @EventHandler
    public void onSpawnerEggChange(SpawnerEggChangeEvent event) {
        Player player = event.getPlayer();

        // Cancel the default behavior to prevent multi-spawner changes
        event.setCancelled(true);

        EntityType oldType = event.getOldEntityType();
        EntityType newType = event.getNewEntityType();

        // Validate held item
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || !smartSpawner.isValidSpawner(inHand)) return;
        if (smartSpawner.getSpawnerEntityType(inHand) != oldType) return;

        // Remove 1 from held stack -- shit
        int remaining = inHand.getAmount() - 1;
        if (remaining <= 0) {
            player.getInventory().setItemInMainHand(null);
        } else {
            inHand.setAmount(remaining);
            player.getInventory().setItemInMainHand(inHand);
        }

        // Create 1 spawner of the new type -- shit
        ItemStack newSpawner = smartSpawner.createSpawnerItem(newType, 1);

        // Safely add to player's inventory -- shit
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(newSpawner);
        if (!leftover.isEmpty()) {
            // Drop the spawner if inventory is full
            player.getWorld().dropItemNaturally(player.getLocation(), newSpawner);
        }
        // works
        player.sendMessage("§a1 " + oldType.name() + " Spawner turned into 1 " + newType.name() + " Spawner!");
    }
}
