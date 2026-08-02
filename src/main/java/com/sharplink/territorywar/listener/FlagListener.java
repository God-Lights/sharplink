package com.sharplink.territorywar.listener;

import com.sharplink.territorywar.TerritoryWarPlugin;
import com.sharplink.territorywar.item.FlagItem;
import com.sharplink.territorywar.model.PlayerRecord;
import com.sharplink.territorywar.model.Team;
import com.sharplink.territorywar.team.TeamManager;
import com.sharplink.territorywar.territory.CellCoord;
import com.sharplink.territorywar.territory.TerritoryManager;
import com.sharplink.territorywar.win.WinConditionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

public final class FlagListener implements Listener {

    private final TerritoryWarPlugin plugin;
    private final TeamManager teamManager;
    private final TerritoryManager territoryManager;
    private final WinConditionManager winConditionManager;
    private final FlagItem flagItem;
    private final String worldName;

    public FlagListener(TerritoryWarPlugin plugin, TeamManager teamManager, TerritoryManager territoryManager,
                         WinConditionManager winConditionManager, FlagItem flagItem) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.territoryManager = territoryManager;
        this.winConditionManager = winConditionManager;
        this.flagItem = flagItem;
        this.worldName = plugin.getConfig().getString("world", "world");
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!flagItem.isFlag(event.getItemInHand())) {
            return;
        }

        Player player = event.getPlayer();

        if (!event.getBlockPlaced().getWorld().getName().equals(worldName)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("영토전쟁이 진행되는 월드(" + worldName + ")에서만 깃발을 설치할 수 있습니다.", NamedTextColor.RED));
            return;
        }

        if (winConditionManager.isGameEnded()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("게임이 이미 종료되었습니다.", NamedTextColor.RED));
            return;
        }

        Location location = event.getBlockPlaced().getLocation();
        PlayerRecord record = teamManager.getOrCreatePlayerRecord(player.getUniqueId());
        CellCoord cell = CellCoord.fromLocation(location, territoryManager.getCellSize());

        if (record.getTeamId() != null) {
            handleClaim(event, player, record, cell);
        } else if (record.isDisplaced()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("무소속(반편입자) 상태에서는 깃발을 설치할 수 없습니다. /team join <팀 이름> 으로 팀에 합류하세요.", NamedTextColor.RED));
        } else {
            handleFounding(event, player, location, cell);
        }
    }

    private void handleFounding(BlockPlaceEvent event, Player player, Location location, CellCoord cell) {
        if (!teamManager.canFoundNewTeam()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("이미 최대 " + teamManager.getMaxTeams() + "개 팀이 창단되었습니다. 곧 기존 팀에 배정됩니다.", NamedTextColor.RED));
            return;
        }
        if (!teamManager.isSpawnFarEnough(location)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("다른 팀의 스폰과 최소 " + (int) teamManager.getMinSpawnDistance() + "m 이상 떨어진 곳에 설치해야 합니다.", NamedTextColor.RED));
            return;
        }

        Team team = teamManager.createTeam(player.getName() + "팀", player, location);
        territoryManager.claim(cell, team.getId());

        player.sendMessage(Component.text("\"" + team.getName() + "\" 팀을 창단했습니다! 이곳이 팀의 스폰이자 첫 영토입니다.", NamedTextColor.GREEN));
    }

    private void handleClaim(BlockPlaceEvent event, Player player, PlayerRecord record, CellCoord cell) {
        Team team = teamManager.getTeam(record.getTeamId());
        UUID owner = territoryManager.getOwner(cell);

        if (owner != null && owner.equals(team.getId())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("이미 우리 팀의 영토입니다.", NamedTextColor.YELLOW));
            return;
        }
        if (owner != null) {
            event.setCancelled(true);
            player.sendMessage(Component.text("적의 영토입니다. 먼저 상대 깃발을 부숴야 합니다.", NamedTextColor.RED));
            return;
        }

        territoryManager.claim(cell, team.getId());
        player.sendMessage(Component.text("영토를 확장했습니다! 현재 " + territoryManager.countCells(team.getId()) + "칸 보유 중.", NamedTextColor.GREEN));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!flagItem.isFlagBlock(block)) {
            return;
        }

        Player breaker = event.getPlayer();

        if (winConditionManager.isGameEnded()) {
            event.setCancelled(true);
            breaker.sendMessage(Component.text("게임이 이미 종료되었습니다.", NamedTextColor.RED));
            return;
        }

        CellCoord cell = CellCoord.fromLocation(block.getLocation(), territoryManager.getCellSize());
        UUID ownerTeamId = territoryManager.getOwner(cell);
        if (ownerTeamId == null) {
            return;
        }

        PlayerRecord breakerRecord = teamManager.getOrCreatePlayerRecord(breaker.getUniqueId());
        UUID breakerTeamId = breakerRecord.getTeamId();

        if (breakerTeamId == null) {
            event.setCancelled(true);
            breaker.sendMessage(Component.text("무소속 상태에서는 깃발을 부술 수 없습니다.", NamedTextColor.RED));
            return;
        }
        if (breakerTeamId.equals(ownerTeamId)) {
            event.setCancelled(true);
            breaker.sendMessage(Component.text("우리 팀의 깃발은 부술 수 없습니다.", NamedTextColor.RED));
            return;
        }
        if (territoryManager.countCells(breakerTeamId) < 1) {
            event.setCancelled(true);
            breaker.sendMessage(Component.text("자신의 팀이 영토를 보유하고 있어야 적의 깃발을 부술 수 있습니다.", NamedTextColor.RED));
            return;
        }

        Team ownerTeam = teamManager.getTeam(ownerTeamId);
        territoryManager.vacate(cell);

        if (ownerTeam != null) {
            plugin.getServer().broadcast(Component.text(
                    breaker.getName() + "님이 \"" + ownerTeam.getName() + "\" 팀의 깃발을 부쉈습니다. 해당 영토는 무주지가 되었습니다.",
                    NamedTextColor.YELLOW));

            if (territoryManager.countCells(ownerTeamId) == 0) {
                Team conqueror = teamManager.getTeam(breakerTeamId);
                teamManager.eliminateTeam(ownerTeam, conqueror);
                plugin.getServer().broadcast(Component.text(
                        "\"" + ownerTeam.getName() + "\" 팀이 모든 영토를 잃고 소멸했습니다. 팀장은 \"" + conqueror.getName() + "\" 팀에 편입되었습니다.",
                        NamedTextColor.GOLD));
                winConditionManager.checkSingleTeamStanding();
            }
        }
    }
}
