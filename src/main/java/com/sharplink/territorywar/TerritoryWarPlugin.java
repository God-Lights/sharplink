package com.sharplink.territorywar;

import com.sharplink.territorywar.command.TeamCommand;
import com.sharplink.territorywar.item.FlagItem;
import com.sharplink.territorywar.listener.FlagListener;
import com.sharplink.territorywar.listener.PlayerJoinListener;
import com.sharplink.territorywar.team.TeamManager;
import com.sharplink.territorywar.territory.TerritoryManager;
import com.sharplink.territorywar.win.WinConditionManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TerritoryWarPlugin extends JavaPlugin {

    private TeamManager teamManager;
    private TerritoryManager territoryManager;
    private WinConditionManager winConditionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.teamManager = new TeamManager(this);
        teamManager.load();

        this.territoryManager = new TerritoryManager(this);
        territoryManager.load();

        this.winConditionManager = new WinConditionManager(this, teamManager, territoryManager);

        FlagItem flagItem = new FlagItem(this);
        getServer().addRecipe(flagItem.createRecipe());

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(teamManager), this);
        getServer().getPluginManager().registerEvents(
                new FlagListener(this, teamManager, territoryManager, winConditionManager, flagItem), this);

        TeamCommand teamCommand = new TeamCommand(teamManager, territoryManager);
        getCommand("team").setExecutor(teamCommand);
        getCommand("team").setTabCompleter(teamCommand);

        winConditionManager.start();

        getLogger().info("영토전쟁 게임이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        if (teamManager != null) {
            teamManager.save();
        }
        if (territoryManager != null) {
            territoryManager.save();
        }
    }
}
