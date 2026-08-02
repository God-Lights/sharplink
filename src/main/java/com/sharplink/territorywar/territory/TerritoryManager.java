package com.sharplink.territorywar.territory;

import com.sharplink.territorywar.TerritoryWarPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class TerritoryManager {

    private final TerritoryWarPlugin plugin;
    private final File dataFile;
    private final Map<CellCoord, UUID> ownership = new ConcurrentHashMap<>();
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
        for (String key : yaml.getKeys(false)) {
            try {
                CellCoord cell = CellCoord.deserialize(key);
                UUID teamId = UUID.fromString(yaml.getString(key));
                ownership.put(cell, teamId);
            } catch (IllegalArgumentException ignored) {
                // 잘못된 항목은 건너뜀
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<CellCoord, UUID> entry : ownership.entrySet()) {
            yaml.set(entry.getKey().serialize(), entry.getValue().toString());
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

    public UUID getOwner(CellCoord cell) {
        return ownership.get(cell);
    }

    public void claim(CellCoord cell, UUID teamId) {
        ownership.put(cell, teamId);
        saveAsync();
    }

    public void vacate(CellCoord cell) {
        ownership.remove(cell);
        saveAsync();
    }

    public long countCells(UUID teamId) {
        return ownership.values().stream().filter(teamId::equals).count();
    }
}
