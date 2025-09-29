package me.universalbank.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomCrafts {

    private final JavaPlugin plugin;

    public CustomCrafts(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerRecipes() {

        // === 1 Quartz Block -> 4 Quartz ===
        ItemStack quartzResult = new ItemStack(Material.QUARTZ, 4);
        NamespacedKey quartzKey = new NamespacedKey(plugin, "quartz_from_block");
        ShapelessRecipe quartzRecipe = new ShapelessRecipe(quartzKey, quartzResult);
        quartzRecipe.addIngredient(Material.QUARTZ_BLOCK);
        Bukkit.addRecipe(quartzRecipe);

        // === 5 Strings in X Pattern -> Cobweb ===
        ItemStack cobwebResult = new ItemStack(Material.COBWEB, 1);
        NamespacedKey cobwebKey = new NamespacedKey(plugin, "cobweb_x_pattern");
        ShapedRecipe cobwebRecipe = new ShapedRecipe(cobwebKey, cobwebResult);
        cobwebRecipe.shape("S S",
                " S ",
                "S S");
        cobwebRecipe.setIngredient('S', Material.STRING);
        Bukkit.addRecipe(cobwebRecipe);

        // === 1 White Wool -> 4 String ===
        ItemStack stringResult = new ItemStack(Material.STRING, 4);
        NamespacedKey stringKey = new NamespacedKey(plugin, "string_from_white_wool");
        ShapelessRecipe stringRecipe = new ShapelessRecipe(stringKey, stringResult);
        stringRecipe.addIngredient(Material.WHITE_WOOL);
        Bukkit.addRecipe(stringRecipe);

        // === Disable Netherite Upgrade Template dupe ===
        try {
            NamespacedKey upgradeKey = NamespacedKey.minecraft("netherite_upgrade_smithing_template");
            Bukkit.removeRecipe(upgradeKey);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not remove netherite upgrade template recipe: " + e.getMessage());
        }
    }
}
