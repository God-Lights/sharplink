package com.sharplink.territorywar.territory;

import com.sharplink.territorywar.TerritoryWarPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class TerritoryManager {

    /** 어느 팀에도 속하지 않는, 서버가 직접 소유하는 중립 영토를 나타내는 예약된 id. */
    public static final UUID SERVER_TEAM_ID = new UUID(0L, 0L);

    private final TerritoryWarPlugin plugin;
    private final File dataFile;
    private final Map<CellCoord, UUID> ownership = new ConcurrentHashMap<>();
    private final Map<CellCoord, BlockPos> markers = new ConcurrentHashMap<>();
    private final int cellSize;

    public TerritoryManager(TerritoryWarPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "territory.yml");
        this.cellSize = plugin.getConfig().getInt("cell-size", 100);
    }

    public int getCellSize() {
        return cellSize;
    }

    public void load() {
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection ownersSection = yaml.getConfigurationSection("owners");
        if (ownersSection != null) {
            for (String key : ownersSection.getKeys(false)) {
                try {
                    CellCoord cell = CellCoord.deserialize(key);
                    UUID teamId = UUID.fromString(ownersSection.getString(key));
                    ownership.put(cell, teamId);
                } catch (IllegalArgumentException ignored) {
                    // 잘못된 항목은 건너뜀
                }
            }
        }

        ConfigurationSection markersSection = yaml.getConfigurationSection("markers");
        if (markersSection != null) {
            for (String key : markersSection.getKeys(false)) {
                try {
                    CellCoord cell = CellCoord.deserialize(key);
                    BlockPos pos = BlockPos.deserialize(markersSection.getString(key));
                    markers.put(cell, pos);
                } catch (IllegalArgumentException ignored) {
                    // 잘못된 항목은 건너뜀
                }
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<CellCoord, UUID> entry : ownership.entrySet()) {
            yaml.set("owners." + entry.getKey().serialize(), entry.getValue().toString());
        }
        for (Map.Entry<CellCoord, BlockPos> entry : markers.entrySet()) {
            yaml.set("markers." + entry.getKey().serialize(), entry.getValue().serialize());
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "영토 데이터를 저장하지 못했습니다.", e);
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
        ownership.clear();
        markers.clear();
        saveAsync();
    }

    public UUID getOwner(CellCoord cell) {
        return ownership.get(cell);
    }

    public BlockPos getMarker(CellCoord cell) {
        return markers.get(cell);
    }

    public List<CellCoord> getCellsOwnedBy(UUID teamId) {
        List<CellCoord> cells = new ArrayList<>();
        for (Map.Entry<CellCoord, UUID> entry : ownership.entrySet()) {
            if (teamId.equals(entry.getValue())) {
                cells.add(entry.getKey());
            }
        }
        return cells;
    }

    public boolean isServerTerritory(Location location) {
        CellCoord cell = CellCoord.fromLocation(location, cellSize);
        return SERVER_TEAM_ID.equals(ownership.get(cell));
    }

    public boolean isClaimed(Location location) {
        CellCoord cell = CellCoord.fromLocation(location, cellSize);
        return ownership.containsKey(cell);
    }

    /**
     * 주어진 위치가 어떤 셀의 "정확한 깃발 설치 좌표"와 일치할 때만 그 셀의 소유 팀을 반환한다.
     * 배너는 아이템 태그가 블록으로 전달되지 않으므로, 블록 재질이 아니라 이 좌표 기록으로 깃발 여부를 판별한다.
     */
    public UUID getOwnerIfExactMarker(Location location) {
        CellCoord cell = CellCoord.fromLocation(location, cellSize);
        BlockPos marker = markers.get(cell);
        if (marker == null || !marker.equals(BlockPos.fromLocation(location))) {
            return null;
        }
        return ownership.get(cell);
    }

    public void claim(CellCoord cell, UUID teamId, Location markerLocation) {
        ownership.put(cell, teamId);
        markers.put(cell, BlockPos.fromLocation(markerLocation));
        saveAsync();
    }

    public void vacate(CellCoord cell) {
        ownership.remove(cell);
        markers.remove(cell);
        saveAsync();
    }

    public CellCoord findCellOwnedBy(UUID teamId) {
        for (Map.Entry<CellCoord, UUID> entry : ownership.entrySet()) {
            if (teamId.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public long countCells(UUID teamId) {
        return ownership.values().stream().filter(teamId::equals).count();
    }
}
