package com.sharplink.lambda;

import com.sharplink.lambda.command.LambdaCommand;
import com.sharplink.lambda.economy.EconomyManager;
import com.sharplink.lambda.item.MasterBookItem;
import com.sharplink.lambda.listener.MasterBookInteractListener;
import com.sharplink.lambda.listener.MasterBookMenuListener;
import com.sharplink.lambda.listener.MasterBookProtectionListener;
import com.sharplink.lambda.listener.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class LambdaPlugin extends JavaPlugin {

    private EconomyManager economyManager;
    private MasterBookItem masterBookItem;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.masterBookItem = new MasterBookItem(this);
        this.economyManager = new EconomyManager(this);
        this.economyManager.load();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, economyManager, masterBookItem), this);
        getServer().getPluginManager().registerEvents(new MasterBookProtectionListener(this, economyManager, masterBookItem), this);
        getServer().getPluginManager().registerEvents(new MasterBookInteractListener(this, economyManager, masterBookItem), this);
        getServer().getPluginManager().registerEvents(new MasterBookMenuListener(this, economyManager), this);

        LambdaCommand lambdaCommand = new LambdaCommand(this, economyManager);
        getCommand("lambda").setExecutor(lambdaCommand);
        getCommand("lambda").setTabCompleter(lambdaCommand);

        getLogger().info("Lambda 화폐 시스템이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.save();
        }
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public MasterBookItem getMasterBookItem() {
        return masterBookItem;
    }
}
