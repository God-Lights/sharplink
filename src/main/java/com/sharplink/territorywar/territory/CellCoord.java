package com.sharplink.territorywar.territory;

import org.bukkit.Location;

public record CellCoord(String world, int cx, int cz) {

    public static CellCoord fromLocation(Location location, int cellSize) {
        int cx = Math.floorDiv((int) Math.floor(location.getX()), cellSize);
        int cz = Math.floorDiv((int) Math.floor(location.getZ()), cellSize);
        return new CellCoord(location.getWorld().getName(), cx, cz);
    }

    public String serialize() {
        return world + ";" + cx + ";" + cz;
    }

    public static CellCoord deserialize(String raw) {
        String[] parts = raw.split(";");
        return new CellCoord(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}
