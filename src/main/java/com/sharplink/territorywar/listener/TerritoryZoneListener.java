package com.sharplink.territorywar.listener;

import com.sharplink.territorywar.model.PlayerRecord;
import com.sharplink.territorywar.model.Team;
import com.sharplink.territorywar.team.TeamManager;
import com.sharplink.territorywar.territory.CellCoord;
import com.sharplink.territorywar.territory.TerritoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 플레이어가 영토 칸 경계를 넘을 때 액션바로 알리고, 적 영토에서는 발광 효과를,
 * 영토 안에 있는 동안에는 발밑에 소유 팀 색깔의 파티클을 보여준다.
 */
public final class TerritoryZoneListener implements Listener {

    private static final int GLOW_DURATION_TICKS = 60;
    private static final long PERIOD_TICKS = 10L;

    private final TeamManager teamManager;
    private final TerritoryManager territoryManager;
    private final String worldName;
    private final Map<UUID, CellCoord> currentCell = new ConcurrentHashMap<>();

    public TerritoryZoneListener(TeamManager teamManager, TerritoryManager territoryManager, String worldName) {
        this.teamManager = teamManager;
        this.territoryManager = territoryManager;
        this.worldName = worldName;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null || !event.getTo().getWorld().getName().equals(worldName)) {
            return;
        }
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        CellCoord newCell = CellCoord.fromLocation(event.getTo(), territoryManager.getCellSize());
        CellCoord oldCell = currentCell.put(player.getUniqueId(), newCell);
        if (newCell.equals(oldCell)) {
            return;
        }

        applyZoneEffects(player, newCell, true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        currentCell.remove(event.getPlayer().getUniqueId());
    }

    private void applyZoneEffects(Player player, CellCoord cell, boolean announce) {
        UUID owner = territoryManager.getOwner(cell);
        if (owner == null) {
            clearGlow(player);
            return;
        }

        PlayerRecord record = teamManager.getOrCreatePlayerRecord(player.getUniqueId());
        boolean isServer = owner.equals(TerritoryManager.SERVER_TEAM_ID);
        boolean isOwn = !isServer && owner.equals(record.getTeamId());
        boolean isEnemy = !isServer && !isOwn;

        if (isEnemy) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, GLOW_DURATION_TICKS, 0, false, false, false));
        } else {
            clearGlow(player);
        }

        if (!announce) {
            return;
        }

        if (isServer) {
            player.sendActionBar(Component.text("안전지대에 들어왔습니다.", NamedTextColor.AQUA));
        } else if (isOwn) {
            int index = territoryManager.getClaimIndex(cell);
            player.sendActionBar(Component.text("내 영토에 들어왔습니다. (" + index + "번째 영토)", NamedTextColor.GREEN));
        } else {
            Team enemyTeam = teamManager.getTeam(owner);
            String enemyName = enemyTeam != null ? enemyTeam.getName() : "알 수 없는 팀";
            player.sendActionBar(Component.text("\"" + enemyName + "\" 팀의 영토입니다!", NamedTextColor.RED));
        }
    }

    private void clearGlow(Player player) {
        if (player.hasPotionEffect(PotionEffectType.GLOWING)) {
            player.removePotionEffect(PotionEffectType.GLOWING);
        }
    }

    /** 정지 상태에서도 발광이 유지되도록 갱신하고, 영토 안에 있는 동안 발밑에 파티클을 뿌린다. */
    public void startPeriodicEffects(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, CellCoord> entry : currentCell.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) {
                    continue;
                }
                CellCoord cell = entry.getValue();
                UUID owner = territoryManager.getOwner(cell);
                if (owner == null) {
                    continue;
                }

                applyZoneEffects(player, cell, false);
                spawnFootParticle(player, owner);
            }
        }, PERIOD_TICKS, PERIOD_TICKS);
    }

    private void spawnFootParticle(Player player, UUID owner) {
        Color color;
        if (owner.equals(TerritoryManager.SERVER_TEAM_ID)) {
            color = Color.AQUA;
        } else {
            PlayerRecord record = teamManager.getOrCreatePlayerRecord(player.getUniqueId());
            color = owner.equals(record.getTeamId()) ? Color.LIME : Color.RED;
        }

        Particle.DustOptions dust = new Particle.DustOptions(color, 1.2f);
        player.spawnParticle(Particle.DUST,
                player.getLocation().getX(), player.getLocation().getY() + 0.1, player.getLocation().getZ(),
                6, 0.4, 0.05, 0.4, 0, dust);
    }
}
