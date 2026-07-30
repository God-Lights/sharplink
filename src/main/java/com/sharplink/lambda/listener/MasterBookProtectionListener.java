package com.sharplink.lambda.listener;

import com.sharplink.lambda.LambdaPlugin;
import com.sharplink.lambda.economy.EconomyManager;
import com.sharplink.lambda.item.MasterBookItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

/**
 * 마스터북을 손 슬롯 옆(오프핸드)에 고정하고, 버리거나 이동/파괴할 수 없도록 막는다.
 */
public final class MasterBookProtectionListener implements Listener {

    private final LambdaPlugin plugin;
    private final EconomyManager economyManager;
    private final MasterBookItem masterBookItem;

    public MasterBookProtectionListener(LambdaPlugin plugin, EconomyManager economyManager, MasterBookItem masterBookItem) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.masterBookItem = masterBookItem;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent event) {
        if (masterBookItem.isMasterBook(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (masterBookItem.isMasterBook(event.getOffHandItem()) || masterBookItem.isMasterBook(event.getMainHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (masterBookItem.isMasterBook(event.getCurrentItem()) || masterBookItem.isMasterBook(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (masterBookItem.isMasterBook(event.getOldCursor())) {
            event.setCancelled(true);
            return;
        }
        for (ItemStack item : event.getNewItems().values()) {
            if (masterBookItem.isMasterBook(item)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            if (masterBookItem.isMasterBook(iterator.next())) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!masterBookItem.isMasterBook(player.getInventory().getItemInOffHand())) {
            player.getInventory().setItemInOffHand(masterBookItem.create());
        }
    }
}
