package com.sharplink.lambda.listener;

import com.sharplink.lambda.LambdaPlugin;
import com.sharplink.lambda.economy.EconomyManager;
import com.sharplink.lambda.item.MasterBookItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public final class PlayerJoinListener implements Listener {

    private final LambdaPlugin plugin;
    private final EconomyManager economyManager;
    private final MasterBookItem masterBookItem;

    public PlayerJoinListener(LambdaPlugin plugin, EconomyManager economyManager, MasterBookItem masterBookItem) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.masterBookItem = masterBookItem;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!economyManager.hasAccount(uuid)) {
            economyManager.createAccount(uuid, economyManager.getCurrencySettings().getStartingBalance());
        }

        PlayerInventory inventory = player.getInventory();
        if (!masterBookItem.isMasterBook(inventory.getItemInOffHand())) {
            inventory.setItemInOffHand(masterBookItem.create());
        }
    }
}
