package com.sharplink.territorywar.task;

import com.sharplink.territorywar.model.PlayerRecord;
import com.sharplink.territorywar.team.TeamManager;
import com.sharplink.territorywar.territory.CellCoord;
import com.sharplink.territorywar.territory.TerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.UUID;

/** 플레이어가 서 있는 칸이 소유된 영토라면, 그 칸의 100x100 경계선을 파티클로 그려 보여준다. */
public final class TerritoryBorderTask implements Runnable {

    private static final int STEP = 4;

    private final TeamManager teamManager;
    private final TerritoryManager territoryManager;
    private final String worldName;

    public TerritoryBorderTask(TeamManager teamManager, TerritoryManager territoryManager, String worldName) {
        this.teamManager = teamManager;
        this.territoryManager = territoryManager;
        this.worldName = worldName;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location location = player.getLocation();
            if (!location.getWorld().getName().equals(worldName)) {
                continue;
            }

            CellCoord cell = CellCoord.fromLocation(location, territoryManager.getCellSize());
            UUID owner = territoryManager.getOwner(cell);
            if (owner == null) {
                continue;
            }

            drawBorder(player, cell, location.getY(), resolveColor(player, owner));
        }
    }

    private Color resolveColor(Player player, UUID owner) {
        if (owner.equals(TerritoryManager.SERVER_TEAM_ID)) {
            return Color.AQUA;
        }
        PlayerRecord record = teamManager.getOrCreatePlayerRecord(player.getUniqueId());
        if (owner.equals(record.getTeamId())) {
            return Color.LIME;
        }
        return Color.RED;
    }

    private void drawBorder(Player player, CellCoord cell, double y, Color color) {
        int cellSize = territoryManager.getCellSize();
        int minX = cell.cx() * cellSize;
        int minZ = cell.cz() * cellSize;
        int maxX = minX + cellSize;
        int maxZ = minZ + cellSize;

        Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);

        for (int x = minX; x <= maxX; x += STEP) {
            spawn(player, x, y, minZ, dust);
            spawn(player, x, y, maxZ, dust);
        }
        for (int z = minZ; z <= maxZ; z += STEP) {
            spawn(player, minX, y, z, dust);
            spawn(player, maxX, y, z, dust);
        }
    }

    private void spawn(Player player, double x, double y, double z, Particle.DustOptions dust) {
        player.spawnParticle(Particle.DUST, x, y + 1, z, 1, 0, 0, 0, 0, dust);
    }
}
