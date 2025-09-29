package me.universalbank.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FortuneDisabler implements Listener {

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        if (event.getEnchantsToAdd().containsKey(Enchantment.FORTUNE)) {
            event.getEnchantsToAdd().remove(Enchantment.FORTUNE);
        }
    }

    @EventHandler
    public void onVillagerTrade(TradeSelectEvent event) {
        Merchant merchant = event.getMerchant();
        if (!(merchant instanceof Villager villager)) return;

        MerchantRecipe recipe = villager.getRecipe(event.getIndex());
        ItemStack result = recipe.getResult();
        if (containsFortune(result)) {
            MerchantRecipe newRecipe = rerollTrade(recipe);
            villager.setRecipe(event.getIndex(), newRecipe);
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result == null) return;

        if (containsFortune(result)) {
            event.setResult(null);
            if (event.getView().getPlayer() instanceof Player p) {
                p.sendMessage(ChatColor.RED + "Fortune cannot be applied.");
            }
        }
    }

    public static void purgeExistingFortune() {
        for (World world : Bukkit.getWorlds()) {
            world.getEntities().forEach(entity -> {
                if (entity instanceof Player p) {
                    cleanseInventory(p.getInventory());
                    cleanseInventory(p.getEnderChest());
                }
            });
        }
    }

    private static void cleanseInventory(Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item == null) continue;
            if (removeFortune(item)) {
                item.setItemMeta(item.getItemMeta());
            }
        }
    }

    private static boolean removeFortune(ItemStack item) {
        boolean modified = false;
        if (item.containsEnchantment(Enchantment.FORTUNE)) {
            item.removeEnchantment(Enchantment.FORTUNE);
            modified = true;
        }
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            if (meta.hasStoredEnchant(Enchantment.FORTUNE)) {
                meta.removeStoredEnchant(Enchantment.FORTUNE);
                item.setItemMeta(meta);
                modified = true;
            }
        }
        return modified;
    }

    private static boolean containsFortune(ItemStack item) {
        if (item == null) return false;
        if (item.containsEnchantment(Enchantment.FORTUNE)) return true;
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            return meta.hasStoredEnchant(Enchantment.FORTUNE);
        }
        return false;
    }

    private static MerchantRecipe rerollTrade(MerchantRecipe old) {
        Random rnd = new Random();
        List<Enchantment> pool = new ArrayList<>();
        for (Enchantment e : Enchantment.values()) {
            if (e.canEnchantItem(new ItemStack(Material.BOOK)) && e != Enchantment.FORTUNE) {
                pool.add(e);
            }
        }
        if (pool.isEmpty()) return old;

        Enchantment chosen = pool.get(rnd.nextInt(pool.size()));
        int level = 1 + rnd.nextInt(chosen.getMaxLevel());

        ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) newBook.getItemMeta();
        meta.addStoredEnchant(chosen, level, true);
        newBook.setItemMeta(meta);

        MerchantRecipe recipe = new MerchantRecipe(newBook, old.getMaxUses());
        recipe.setExperienceReward(old.hasExperienceReward());
        recipe.setIngredients(old.getIngredients());
        recipe.setPriceMultiplier(old.getPriceMultiplier());
        return recipe;
    }
}
