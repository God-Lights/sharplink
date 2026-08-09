package com.sharplink.territorywar.territory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * 블록 단위의 정확한 좌표. 배너는 아이템에 붙인 태그(PersistentDataContainer)가
 * 설치된 블록으로 넘어가지 않으므로, 어떤 블록이 "그 셀의 깃발"인지 좌표로 직접 기억해서 판별한다.
 */
public record BlockPos(String world, int x, int y, int z) {

    public static BlockPos fromLocation(Location location) {
        return new BlockPos(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Location toLocation() {
        World bukkitWorld = Bukkit.getWorld(world);
        return new Location(bukkitWorld, x + 0.5, y, z + 0.5);
    }

    public String serialize() {
        return world + ";" + x + ";" + y + ";" + z;
    }

    public static BlockPos deserialize(String raw) {
        String[] parts = raw.split(";");
        return new BlockPos(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }
}
