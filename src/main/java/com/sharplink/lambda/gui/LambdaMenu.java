package com.sharplink.lambda.gui;

import com.sharplink.lambda.economy.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class LambdaMenu {

    public static final Component TITLE = Component.text("람다 정보", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD);

    public static final int INFO_SLOT = 2;
    public static final int SHOP_SLOT = 6;

    private LambdaMenu() {
    }

    public static void open(Player player, EconomyManager economyManager) {
        LambdaMenuHolder holder = new LambdaMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 9, TITLE);
        holder.setInventory(inventory);

        long balance = economyManager.getBalance(player.getUniqueId());
        inventory.setItem(INFO_SLOT, buildItem(Material.EMERALD, "내 정보", List.of(
                Component.text("보유 잔액: " + economyManager.getCurrencySettings().format(balance), NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        )));

        inventory.setItem(SHOP_SLOT, buildItem(Material.CHEST, "상점 만들기", List.of(
                Component.text("준비 중인 기능입니다.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        )));

        player.openInventory(inventory);
    }

    private static ItemStack buildItem(Material material, String name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.AQUA, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
