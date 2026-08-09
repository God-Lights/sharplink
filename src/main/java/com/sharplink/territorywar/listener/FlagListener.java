package com.sharplink.territorywar.listener;

import com.sharplink.territorywar.TerritoryWarPlugin;
import com.sharplink.territorywar.game.GameManager;
import com.sharplink.territorywar.item.FlagItem;
import com.sharplink.territorywar.item.ServerFlagItem;
import com.sharplink.territorywar.model.PlayerRecord;
import com.sharplink.territorywar.model.Team;
import com.sharplink.territorywar.team.TeamManager;
import com.sharplink.territorywar.territory.CellCoord;
import com.sharplink.territorywar.territory.TerritoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class FlagListener implements Listener {

    private final TerritoryWarPlugin plugin;
    private final TeamManager teamManager;
    private final TerritoryManager territoryManager;
    private final GameManager gameManager;
    private final FlagItem flagItem;
    private final ServerFlagItem serverFlagItem;
    private final String worldName;

    public FlagListener(TerritoryWarPlugin plugin, TeamManager teamManager, TerritoryManager territoryManager,
                         GameManager gameManager, FlagItem flagItem, ServerFlagItem serverFlagItem) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.territoryManager = territoryManager;
        this.gameManager = gameManager;
        this.flagItem = flagItem;
        this.serverFlagItem = serverFlagItem;
        this.worldName = plugin.getConfig().getString("world", "world");
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        boolean isTeamFlag = flagItem.isFlag(item);
        boolean isServerFlag = serverFlagItem.isServerFlag(item);
        if (!isTeamFlag && !isServerFlag) {
            return;
        }

        Player player = event.getPlayer();
        Location location = event.getBlockPlaced().getLocation();

        if (!location.getWorld().getName().equals(worldName)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("영토전쟁이 진행되는 월드(" + worldName + ")에서만 깃발을 설치할 수 있습니다.", NamedTextColor.RED));
            return;
        }

        CellCoord cell = CellCoord.fromLocation(location, territoryManager.getCellSize());

        if (isServerFlag) {
            handleServerFlagPlace(event, player, location, cell);
            return;
        }

        if (gameManager.isEnded()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("게임이 이미 종료되었습니다.", NamedTextColor.RED));
            return;
        }
        if (!gameManager.isRunning() && !gameManager.isDebugMode()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("게임이 아직 시작되지 않았습니다. 관리자가 /game start로 시작해야 합니다.", NamedTextColor.RED));
            return;
        }

        PlayerRecord record = teamManager.getOrCreatePlayerRecord(player.getUniqueId());

        if (record.getTeamId() != null) {
            handleClaim(event, player, record, cell, location);
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
        if (!gameManager.isDebugMode() && !teamManager.isSpawnFarEnough(location)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("다른 팀의 스폰과 최소 " + (int) teamManager.getMinSpawnDistance() + "m 이상 떨어진 곳에 설치해야 합니다.", NamedTextColor.RED));
            return;
        }

        Team team = teamManager.createTeam(player.getName() + "팀", player, location);
        territoryManager.claim(cell, team.getId(), location);
        anchorBedrockBelow(location);

        player.sendMessage(Component.text("\"" + team.getName() + "\" 팀을 창단했습니다! 이곳이 팀의 스폰이자 첫 영토입니다.", NamedTextColor.GREEN));
    }

    private void handleClaim(BlockPlaceEvent event, Player player, PlayerRecord record, CellCoord cell, Location location) {
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

        territoryManager.claim(cell, team.getId(), location);
        anchorBedrockBelow(location);
        player.sendMessage(Component.text("영토를 확장했습니다! 현재 " + territoryManager.countCells(team.getId()) + "칸 보유 중.", NamedTextColor.GREEN));
    }

    private void handleServerFlagPlace(BlockPlaceEvent event, Player player, Location location, CellCoord cell) {
        if (!player.hasPermission("territorywar.admin")) {
            event.setCancelled(true);
            player.sendMessage(Component.text("서버 깃발은 관리자만 설치할 수 있습니다.", NamedTextColor.RED));
            return;
        }

        CellCoord existing = territoryManager.findCellOwnedBy(TerritoryManager.SERVER_TEAM_ID);
        if (existing != null && !existing.equals(cell)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("이미 서버 깃발이 설치되어 있습니다. 먼저 기존 서버 깃발을 제거하세요.", NamedTextColor.RED));
            return;
        }

        territoryManager.claim(cell, TerritoryManager.SERVER_TEAM_ID, location);
        anchorBedrockBelow(location);
        location.getWorld().setSpawnLocation(location);

        plugin.getServer().broadcast(Component.text("서버 깃발이 설치되었습니다. 이곳이 서버 스폰이자 서버 영토입니다.", NamedTextColor.AQUA));
    }

    private void handleServerFlagBreak(BlockBreakEvent event, Player breaker, CellCoord cell) {
        if (!breaker.hasPermission("territorywar.admin")) {
            event.setCancelled(true);
            breaker.sendMessage(Component.text("서버 깃발은 관리자만 파괴할 수 있습니다.", NamedTextColor.RED));
            return;
        }

        territoryManager.vacate(cell);
        plugin.getServer().broadcast(Component.text("서버 깃발이 제거되었습니다.", NamedTextColor.AQUA));
    }

    /** 지지 블록을 캐서 깃발이 물리적으로 뜯겨나가는 것을 막는다. */
    private void anchorBedrockBelow(Location flagLocation) {
        flagLocation.getBlock().getRelative(0, -1, 0).setType(Material.BEDROCK);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        CellCoord cell = CellCoord.fromLocation(block.getLocation(), territoryManager.getCellSize());
        UUID ownerTeamId = territoryManager.getOwnerIfExactMarker(block.getLocation());
        if (ownerTeamId == null) {
            return;
        }

        Player breaker = event.getPlayer();

        if (ownerTeamId.equals(TerritoryManager.SERVER_TEAM_ID)) {
            handleServerFlagBreak(event, breaker, cell);
            return;
        }

        if (gameManager.isEnded()) {
            event.setCancelled(true);
            breaker.sendMessage(Component.text("게임이 이미 종료되었습니다.", NamedTextColor.RED));
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
                gameManager.checkSingleTeamStanding();
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        UUID ownerTeamId = territoryManager.getOwnerIfExactMarker(block.getLocation());
        if (ownerTeamId == null) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        long cells = territoryManager.countCells(ownerTeamId);
        String ownerName;
        if (ownerTeamId.equals(TerritoryManager.SERVER_TEAM_ID)) {
            ownerName = "서버";
        } else {
            Team team = teamManager.getTeam(ownerTeamId);
            ownerName = team != null ? team.getName() : "알 수 없음";
        }

        player.sendMessage(Component.text("\"" + ownerName + "\" 영토 — 보유 " + cells + "칸", NamedTextColor.AQUA));

        player.setCompassTarget(block.getLocation());
        if (!player.getInventory().contains(Material.COMPASS)) {
            player.getInventory().addItem(new ItemStack(Material.COMPASS));
            player.sendMessage(Component.text("웨이포인트가 설정되었습니다. 나침반을 지급했으니 그 방향을 보고 따라가세요.", NamedTextColor.GRAY));
        } else {
            player.sendMessage(Component.text("웨이포인트가 설정되었습니다. 나침반이 이 깃발을 가리킵니다.", NamedTextColor.GRAY));
        }
    }
}
