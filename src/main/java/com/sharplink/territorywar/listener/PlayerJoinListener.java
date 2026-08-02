package com.sharplink.territorywar.listener;

import com.sharplink.territorywar.model.Team;
import com.sharplink.territorywar.team.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {

    private final TeamManager teamManager;

    public PlayerJoinListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (teamManager.hasPlayerRecord(player.getUniqueId())) {
            return;
        }

        teamManager.getOrCreatePlayerRecord(player.getUniqueId());

        if (teamManager.canFoundNewTeam()) {
            player.sendMessage(Component.text("깃발을 제작해서 원하는 곳에 설치하면 팀을 창단할 수 있습니다. (다른 팀 스폰과 최소 "
                    + (int) teamManager.getMinSpawnDistance() + "m 이상 떨어져야 함)", NamedTextColor.AQUA));
        } else {
            Team team = teamManager.assignRandomTeam(player);
            player.teleport(team.getSpawnPoint().toLocation());
            player.sendMessage(Component.text("이미 " + teamManager.getMaxTeams() + "개 팀이 모두 창단되어 \""
                    + team.getName() + "\" 팀에 반편입자로 배정되었습니다.", NamedTextColor.AQUA));
        }
    }
}
