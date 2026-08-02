package com.sharplink.territorywar.model;

import java.util.UUID;

/**
 * 플레이어 한 명의 소속 상태.
 * team과 role이 모두 null이면 아직 팀을 창단하지 않은 신규 플레이어(창단 가능 상태).
 * displacedBlocId가 있으면 팀 소멸로 무소속이 된 상태로, 같은 값을 가진 사람들끼리만 뭉쳐서
 * 새 팀에 합류할 수 있다.
 */
public final class PlayerRecord {

    private final UUID uuid;
    private UUID teamId;
    private PlayerRole role;
    private UUID displacedBlocId;

    public PlayerRecord(UUID uuid, UUID teamId, PlayerRole role, UUID displacedBlocId) {
        this.uuid = uuid;
        this.teamId = teamId;
        this.role = role;
        this.displacedBlocId = displacedBlocId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public UUID getDisplacedBlocId() {
        return displacedBlocId;
    }

    public void setDisplacedBlocId(UUID displacedBlocId) {
        this.displacedBlocId = displacedBlocId;
    }

    /** 팀이 소멸해 무소속이 되어, 같은 무리와 함께만 새 팀에 합류할 수 있는 상태인지. */
    public boolean isDisplaced() {
        return displacedBlocId != null;
    }
}
