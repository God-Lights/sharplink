package com.sharplink.territorywar.listener;

import com.sharplink.territorywar.gui.FlagListMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public final class FlagListMenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof FlagListMenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null || event.getClickedInventory() != top) {
            return;
        }

        Location target = holder.getTarget(event.getSlot());
        if (target == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        player.setCompassTarget(target);
        player.closeInventory();
        player.sendMessage(Component.text("웨이포인트가 설정되었습니다. 나침반이 그 위치를 가리킵니다.", NamedTextColor.GRAY));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof FlagListMenuHolder) {
            event.setCancelled(true);
        }
    }
}
