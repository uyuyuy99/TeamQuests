package me.uyuyuy99.teamquests.util;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.uyuyuy99.teamquests.Config;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemUtil {

    public static void addGlow(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 69);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    public static void setItemName(ItemStack item, String name) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(CC.translate(name));
        item.setItemMeta(itemMeta);
    }

    public static void setItemLore(ItemStack item, List<String> lore) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(CC.translate(lore));
        item.setItemMeta(itemMeta);
    }

    public static void hideItemAttributes(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta != null) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(itemMeta);
        }
    }

    public static ItemStack getIconFromConfig(ConfigurationSection section, String key) {
        ItemStack item = new ItemStack(
                Material.valueOf(section.getString(key).toUpperCase()),
                section.getInt(key + "-amount", 1)
        );

        if (section.getBoolean(key + "-glow", false)) {
            addGlow(item);
        }

        hideItemAttributes(item);
        return item;
    }
    public static ItemStack getIconFromConfig(Section section, String key) {
        ItemStack item = new ItemStack(
                Material.valueOf(section.getString(key).toUpperCase()),
                section.getInt(key + "-amount", 1)
        );

        if (section.getBoolean(key + "-glow", false)) {
            addGlow(item);
        }

        hideItemAttributes(item);
        return item;
    }

}
