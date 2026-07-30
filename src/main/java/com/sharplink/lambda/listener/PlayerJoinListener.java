package com.sharplink.lambda.listener;

import com.sharplink.lambda.LambdaPlugin;
import com.sharplink.lambda.economy.EconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public final class PlayerJoinListener implements Listener {

    private final LambdaPlugin plugin;
    private final EconomyManager economyManager;

    public PlayerJoinListener(LambdaPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!economyManager.hasAccount(uuid)) {
            economyManager.createAccount(uuid, economyManager.getCurrencySettings().getStartingBalance());
        }
    }
}
