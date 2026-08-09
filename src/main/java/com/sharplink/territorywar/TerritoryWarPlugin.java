package com.sharplink.territorywar;

import com.sharplink.territorywar.command.FlagCommand;
import com.sharplink.territorywar.command.GameCommand;
import com.sharplink.territorywar.command.TeamCommand;
import com.sharplink.territorywar.game.GameManager;
import com.sharplink.territorywar.item.FlagItem;
import com.sharplink.territorywar.item.ServerFlagItem;
import com.sharplink.territorywar.listener.FlagListener;
import com.sharplink.territorywar.listener.FlagListMenuListener;
import com.sharplink.territorywar.listener.MonsterProtectionListener;
import com.sharplink.territorywar.listener.PlayerJoinListener;
import com.sharplink.territorywar.listener.SafeZoneListener;
import com.sharplink.territorywar.listener.TeamChatListener;
import com.sharplink.territorywar.task.TerritoryBorderTask;
import com.sharplink.territorywar.team.TeamManager;
import com.sharplink.territorywar.territory.TerritoryManager;
import com.sharplink.territorywar.win.WinConditionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TerritoryWarPlugin extends JavaPlugin {

    private TeamManager teamManager;
    private TerritoryManager territoryManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.teamManager = new TeamManager(this);
        teamManager.load();

        this.territoryManager = new TerritoryManager(this);
        territoryManager.load();

        WinConditionManager winConditionManager = new WinConditionManager(this, teamManager, territoryManager);
        GameManager gameManager = new GameManager(teamManager, territoryManager, winConditionManager);
        winConditionManager.setGameManager(gameManager);

        FlagItem flagItem = new FlagItem(this);
        getServer().addRecipe(flagItem.createRecipe());
        ServerFlagItem serverFlagItem = new ServerFlagItem(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(teamManager), this);
        getServer().getPluginManager().registerEvents(
                new FlagListener(this, teamManager, territoryManager, gameManager, flagItem, serverFlagItem), this);
        getServer().getPluginManager().registerEvents(new TeamChatListener(teamManager, territoryManager), this);
        getServer().getPluginManager().registerEvents(new SafeZoneListener(territoryManager), this);
        getServer().getPluginManager().registerEvents(new FlagListMenuListener(), this);

        String worldName = getConfig().getString("world", "world");
        MonsterProtectionListener monsterProtectionListener = new MonsterProtectionListener(territoryManager, worldName);
        getServer().getPluginManager().registerEvents(monsterProtectionListener, this);
        monsterProtectionListener.startPeriodicPurge(this);

        Bukkit.getScheduler().runTaskTimer(this, new TerritoryBorderTask(teamManager, territoryManager, worldName), 20L, 20L);

        TeamCommand teamCommand = new TeamCommand(teamManager, territoryManager);
        getCommand("team").setExecutor(teamCommand);
        getCommand("team").setTabCompleter(teamCommand);

        FlagCommand flagCommand = new FlagCommand(flagItem, serverFlagItem);
        getCommand("flag").setExecutor(flagCommand);
        getCommand("flag").setTabCompleter(flagCommand);

        GameCommand gameCommand = new GameCommand(gameManager);
        getCommand("game").setExecutor(gameCommand);
        getCommand("game").setTabCompleter(gameCommand);

        getLogger().info("영토전쟁 게임이 활성화되었습니다. /game start로 게임을 시작하세요.");
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
