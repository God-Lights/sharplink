package com.sharplink.territorywar.game;

import com.sharplink.territorywar.team.TeamManager;
import com.sharplink.territorywar.territory.TerritoryManager;
import com.sharplink.territorywar.win.WinConditionManager;

public final class GameManager {

    public enum GameState {
        NOT_STARTED,
        RUNNING,
        ENDED
    }

    private final TeamManager teamManager;
    private final TerritoryManager territoryManager;
    private final WinConditionManager winConditionManager;

    private GameState state = GameState.NOT_STARTED;
    private boolean debugMode = false;

    public GameManager(TeamManager teamManager, TerritoryManager territoryManager, WinConditionManager winConditionManager) {
        this.teamManager = teamManager;
        this.territoryManager = territoryManager;
        this.winConditionManager = winConditionManager;
    }

    public GameState getState() {
        return state;
    }

    public boolean isRunning() {
        return state == GameState.RUNNING;
    }

    public boolean isEnded() {
        return state == GameState.ENDED;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /** @return 시작에 성공하면 true, 이미 시작했거나 종료된 상태라 시작할 수 없으면 false */
    public boolean start() {
        if (state != GameState.NOT_STARTED) {
            return false;
        }
        state = GameState.RUNNING;
        winConditionManager.start();
        return true;
    }

    /** WinConditionManager가 승리 조건을 판정해 게임을 종료시킬 때 호출한다. */
    public void markEnded() {
        state = GameState.ENDED;
    }

    /** 게임을 강제로 중단하고 팀/영토 데이터를 모두 지운 뒤 시작 전 상태로 되돌린다. */
    public void reset() {
        winConditionManager.cancelTimer();
        teamManager.resetAll();
        territoryManager.resetAll();
        state = GameState.NOT_STARTED;
    }

    public void checkSingleTeamStanding() {
        winConditionManager.checkSingleTeamStanding();
    }
}
