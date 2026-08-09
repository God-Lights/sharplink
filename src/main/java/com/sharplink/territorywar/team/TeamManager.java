package com.sharplink.territorywar.team;

import com.sharplink.territorywar.TerritoryWarPlugin;
import com.sharplink.territorywar.model.PlayerRecord;
import com.sharplink.territorywar.model.PlayerRole;
import com.sharplink.territorywar.model.SpawnPoint;
import com.sharplink.territorywar.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class TeamManager {

    private final TerritoryWarPlugin plugin;
    private final File dataFile;
    private final Map<UUID, Team> teams = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerRecord> players = new ConcurrentHashMap<>();

    public TeamManager(TerritoryWarPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "teams.yml");
    }

    public int getMaxTeams() {
        return plugin.getConfig().getInt("max-teams", 6);
    }

    public double getMinSpawnDistance() {
        return plugin.getConfig().getDouble("min-team-spawn-distance", 500);
    }

    public void load() {
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection teamsSection = yaml.getConfigurationSection("teams");
        if (teamsSection != null) {
            for (String key : teamsSection.getKeys(false)) {
                ConfigurationSection section = teamsSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                UUID id = UUID.fromString(key);
                String name = section.getString("name", key);
                UUID leader = UUID.fromString(section.getString("leader"));
                SpawnPoint spawn = new SpawnPoint(
                        section.getString("spawn.world"),
                        section.getDouble("spawn.x"),
                        section.getDouble("spawn.y"),
                        section.getDouble("spawn.z"));
                Team team = new Team(id, name, leader, spawn);
                team.getMembers().clear();
                for (String memberUuid : section.getStringList("members")) {
                    team.getMembers().add(UUID.fromString(memberUuid));
                }
                teams.put(id, team);
            }
        }

        ConfigurationSection playersSection = yaml.getConfigurationSection("players");
        if (playersSection != null) {
            for (String key : playersSection.getKeys(false)) {
                ConfigurationSection section = playersSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                UUID uuid = UUID.fromString(key);
                String teamIdRaw = section.getString("team", "");
                String roleRaw = section.getString("role", "");
                String blocRaw = section.getString("displacedBloc", "");

                UUID teamId = teamIdRaw.isEmpty() ? null : UUID.fromString(teamIdRaw);
                PlayerRole role = roleRaw.isEmpty() ? null : PlayerRole.valueOf(roleRaw);
                UUID blocId = blocRaw.isEmpty() ? null : UUID.fromString(blocRaw);

                players.put(uuid, new PlayerRecord(uuid, teamId, role, blocId));
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();

        for (Team team : teams.values()) {
            String base = "teams." + team.getId();
            yaml.set(base + ".name", team.getName());
            yaml.set(base + ".leader", team.getLeaderUuid().toString());
            yaml.set(base + ".members", team.getMembers().stream().map(UUID::toString).toList());
            yaml.set(base + ".spawn.world", team.getSpawnPoint().world());
            yaml.set(base + ".spawn.x", team.getSpawnPoint().x());
            yaml.set(base + ".spawn.y", team.getSpawnPoint().y());
            yaml.set(base + ".spawn.z", team.getSpawnPoint().z());
        }

        for (PlayerRecord record : players.values()) {
            String base = "players." + record.getUuid();
            yaml.set(base + ".team", record.getTeamId() == null ? "" : record.getTeamId().toString());
            yaml.set(base + ".role", record.getRole() == null ? "" : record.getRole().name());
            yaml.set(base + ".displacedBloc", record.getDisplacedBlocId() == null ? "" : record.getDisplacedBlocId().toString());
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "팀 데이터를 저장하지 못했습니다.", e);
        }
    }

    private void saveAsync() {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::save);
        } else {
            save();
        }
    }

    public void resetAll() {
        teams.clear();
        players.clear();
        saveAsync();
    }

    public PlayerRecord getOrCreatePlayerRecord(UUID uuid) {
        return players.computeIfAbsent(uuid, id -> new PlayerRecord(id, null, null, null));
    }

    public boolean hasPlayerRecord(UUID uuid) {
        return players.containsKey(uuid);
    }

    public Collection<Team> getActiveTeams() {
        return teams.values();
    }

    public boolean canFoundNewTeam() {
        return teams.size() < getMaxTeams();
    }

    public Team getTeam(UUID teamId) {
        return teams.get(teamId);
    }

    public Team getTeamByName(String name) {
        return teams.values().stream()
                .filter(team -> team.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isSpawnFarEnough(Location candidate) {
        double minDistance = getMinSpawnDistance();
        for (Team team : teams.values()) {
            if (!team.getSpawnPoint().world().equals(candidate.getWorld().getName())) {
                continue;
            }
            if (team.getSpawnPoint().horizontalDistance(candidate) < minDistance) {
                return false;
            }
        }
        return true;
    }

    public Team createTeam(String name, Player founder, Location spawnLocation) {
        UUID id = UUID.randomUUID();
        Team team = new Team(id, name, founder.getUniqueId(), SpawnPoint.fromLocation(spawnLocation));
        teams.put(id, team);

        PlayerRecord record = getOrCreatePlayerRecord(founder.getUniqueId());
        record.setTeamId(id);
        record.setRole(PlayerRole.LEADER);
        record.setDisplacedBlocId(null);

        saveAsync();
        return team;
    }

    public Team assignRandomTeam(Player player) {
        List<Team> activeTeams = new ArrayList<>(teams.values());
        Team target = activeTeams.get(ThreadLocalRandom.current().nextInt(activeTeams.size()));

        PlayerRecord record = getOrCreatePlayerRecord(player.getUniqueId());
        record.setTeamId(target.getId());
        record.setRole(PlayerRole.SEMI_MEMBER);
        record.setDisplacedBlocId(null);
        target.getMembers().add(player.getUniqueId());

        saveAsync();
        return target;
    }

    /**
     * 팀 소멸 처리: 팀장은 conqueror 팀에 정식 편입되고, 나머지 팀원은 무소속 반편입자가 되어
     * loser 팀 id를 무리 표식(displacedBlocId)으로 공유한다.
     */
    public void eliminateTeam(Team loser, Team conqueror) {
        UUID leaderUuid = loser.getLeaderUuid();

        PlayerRecord leaderRecord = getOrCreatePlayerRecord(leaderUuid);
        leaderRecord.setTeamId(conqueror.getId());
        leaderRecord.setRole(PlayerRole.MEMBER);
        leaderRecord.setDisplacedBlocId(null);
        conqueror.getMembers().add(leaderUuid);

        for (UUID memberUuid : loser.getMembers()) {
            if (memberUuid.equals(leaderUuid)) {
                continue;
            }
            PlayerRecord record = getOrCreatePlayerRecord(memberUuid);
            record.setTeamId(null);
            record.setRole(PlayerRole.SEMI_MEMBER);
            record.setDisplacedBlocId(loser.getId());
        }

        teams.remove(loser.getId());
        saveAsync();
    }

    public List<PlayerRecord> getBlocMembers(UUID blocId) {
        return players.values().stream()
                .filter(record -> blocId.equals(record.getDisplacedBlocId()))
                .collect(Collectors.toList());
    }

    /** 무소속 반편입자 무리 전원을 target 팀으로 함께 합류시킨다. */
    public void joinBloc(UUID blocId, Team target) {
        for (PlayerRecord record : getBlocMembers(blocId)) {
            record.setTeamId(target.getId());
            record.setRole(PlayerRole.SEMI_MEMBER);
            record.setDisplacedBlocId(null);
            target.getMembers().add(record.getUuid());
        }
        saveAsync();
    }
}
