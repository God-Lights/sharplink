package com.sharplink.territorywar.win;

import com.sharplink.territorywar.TerritoryWarPlugin;
import com.sharplink.territorywar.game.GameManager;
import com.sharplink.territorywar.model.Team;
import com.sharplink.territorywar.team.TeamManager;
import com.sharplink.territorywar.territory.TerritoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public final class WinConditionManager {

    private final TerritoryWarPlugin plugin;
    private final TeamManager teamManager;
    private final TerritoryManager territoryManager;

    private GameManager gameManager;
    private BukkitTask timeoutTask;

    public WinConditionManager(TerritoryWarPlugin plugin, TeamManager teamManager, TerritoryManager territoryManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.territoryManager = territoryManager;
    }

    /** GameManager와는 서로를 참조하므로, 생성 이후에 연결한다. */
    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void start() {
        long durationMinutes = plugin.getConfig().getLong("max-game-duration-minutes", 10080);
        long delayTicks = durationMinutes * 60L * 20L;
        this.timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, this::endByTimeout, delayTicks);
    }

    public void cancelTimer() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
    }

    /** 팀 소멸 처리 직후 호출: 살아있는 팀이 하나뿐이면 그 팀의 완전 정복으로 즉시 종료한다. */
    public void checkSingleTeamStanding() {
        if (gameManager.isEnded()) {
            return;
        }
        Collection<Team> activeTeams = teamManager.getActiveTeams();
        if (activeTeams.size() == 1) {
            Team winner = activeTeams.iterator().next();
            endGame(winner, "전 영토 독점");
        }
    }

    private void endByTimeout() {
        if (gameManager.isEnded()) {
            return;
        }
        Optional<Team> winner = teamManager.getActiveTeams().stream()
                .max(Comparator.comparingLong(team -> territoryManager.countCells(team.getId())));

        if (winner.isPresent()) {
            endGame(winner.get(), "제한 시간 종료 시점 영토 최다 보유");
        } else {
            gameManager.markEnded();
            Bukkit.broadcast(Component.text("영토전쟁 게임이 종료되었습니다. (승리 팀 없음)", NamedTextColor.GOLD));
        }
    }

    private void endGame(Team winner, String reason) {
        gameManager.markEnded();
        cancelTimer();
        Bukkit.broadcast(Component.text(
                "영토전쟁 게임 종료! 승리 팀: " + winner.getName() + " (" + reason + ")",
                NamedTextColor.GOLD));
    }
}
