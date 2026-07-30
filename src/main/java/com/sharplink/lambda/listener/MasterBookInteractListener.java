package com.sharplink.lambda.listener;

import com.sharplink.lambda.LambdaPlugin;
import com.sharplink.lambda.economy.EconomyManager;
import com.sharplink.lambda.gui.MasterBookMenu;
import com.sharplink.lambda.item.MasterBookItem;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class MasterBookInteractListener implements Listener {

    private final LambdaPlugin plugin;
    private final EconomyManager economyManager;
    private final MasterBookItem masterBookItem;

    public MasterBookInteractListener(LambdaPlugin plugin, EconomyManager economyManager, MasterBookItem masterBookItem) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.masterBookItem = masterBookItem;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }
        if (!masterBookItem.isMasterBook(event.getItem())) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);

        MasterBookMenu.open(event.getPlayer(), economyManager);
    }
}
