package com.sharplink.territorywar.listener;

import com.sharplink.territorywar.model.PlayerRecord;
import com.sharplink.territorywar.team.TeamManager;
import com.sharplink.territorywar.territory.TerritoryManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * 팀이 있는 플레이어의 채팅은 같은 팀원에게만 보이도록 제한한다.
 * 무소속 상태(창단 전 또는 반편입자)의 채팅이나, 안전지대(서버 영토)에서 보낸 채팅은
 * 제한 없이 전체 공개된다.
 */
public final class TeamChatListener implements Listener {

    private final TeamManager teamManager;
    private final TerritoryManager territoryManager;

    public TeamChatListener(TeamManager teamManager, TerritoryManager territoryManager) {
        this.teamManager = teamManager;
        this.territoryManager = territoryManager;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        if (territoryManager.isServerTerritory(sender.getLocation())) {
            return;
        }

        PlayerRecord record = teamManager.getOrCreatePlayerRecord(sender.getUniqueId());
        UUID teamId = record.getTeamId();
        if (teamId == null) {
            return;
        }

        event.viewers().removeIf(viewer -> {
            if (!(viewer instanceof Player player)) {
                return false;
            }
            if (player.getUniqueId().equals(sender.getUniqueId())) {
                return false;
            }
            PlayerRecord viewerRecord = teamManager.getOrCreatePlayerRecord(player.getUniqueId());
            return !teamId.equals(viewerRecord.getTeamId());
        });
    }
}
