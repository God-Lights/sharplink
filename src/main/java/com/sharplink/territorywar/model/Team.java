package com.sharplink.territorywar.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class Team {

    private final UUID id;
    private String name;
    private UUID leaderUuid;
    private final Set<UUID> members = new LinkedHashSet<>();
    private SpawnPoint spawnPoint;

    public Team(UUID id, String name, UUID leaderUuid, SpawnPoint spawnPoint) {
        this.id = id;
        this.name = name;
        this.leaderUuid = leaderUuid;
        this.spawnPoint = spawnPoint;
        this.members.add(leaderUuid);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getLeaderUuid() {
        return leaderUuid;
    }

    public void setLeaderUuid(UUID leaderUuid) {
        this.leaderUuid = leaderUuid;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public SpawnPoint getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(SpawnPoint spawnPoint) {
        this.spawnPoint = spawnPoint;
    }
}
