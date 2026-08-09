package com.sharplink.territorywar.gui;

import com.sharplink.territorywar.territory.BlockPos;
import com.sharplink.territorywar.territory.CellCoord;
import com.sharplink.territorywar.territory.TerritoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/** 한 팀이 보유한 모든 깃발 위치를 한 화면에서 보여주는 창. */
public final class FlagListMenu {

    private static final int MAX_ENTRIES = 54;

    private FlagListMenu() {
    }

    public static void open(Player player, String teamName, TerritoryManager territoryManager, List<CellCoord> cells) {
        Component title = Component.text("\"" + teamName + "\" 깃발 목록", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD);

        int shown = Math.min(cells.size(), MAX_ENTRIES);
        int size = Math.max(9, ((shown + 8) / 9) * 9);

        FlagListMenuHolder holder = new FlagListMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);

        int slot = 0;
        for (CellCoord cell : cells) {
            if (slot >= MAX_ENTRIES) {
                break;
            }
            BlockPos marker = territoryManager.getMarker(cell);
            if (marker == null) {
                continue;
            }

            Location location = marker.toLocation();
            ItemStack item = new ItemStack(Material.LIME_BANNER);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("X: " + marker.x() + ", Z: " + marker.z(), NamedTextColor.AQUA, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("클릭하면 나침반이 이 위치를 가리킵니다.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
            holder.setTarget(slot, location);
            slot++;
        }

        player.openInventory(inventory);
    }
}
