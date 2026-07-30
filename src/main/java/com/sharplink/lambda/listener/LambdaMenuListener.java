package com.sharplink.lambda.listener;

import com.sharplink.lambda.LambdaPlugin;
import com.sharplink.lambda.economy.EconomyManager;
import com.sharplink.lambda.gui.LambdaMenu;
import com.sharplink.lambda.gui.LambdaMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public final class LambdaMenuListener implements Listener {

    private final LambdaPlugin plugin;
    private final EconomyManager economyManager;

    public LambdaMenuListener(LambdaPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof LambdaMenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null || event.getClickedInventory() != top) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == LambdaMenu.SHOP_SLOT) {
            player.closeInventory();
            player.sendMessage(Component.text("상점 시스템은 추후 업데이트에서 지원될 예정입니다.", NamedTextColor.YELLOW));
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof LambdaMenuHolder) {
            event.setCancelled(true);
        }
    }
}
