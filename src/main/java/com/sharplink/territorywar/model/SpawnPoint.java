package com.sharplink.territorywar.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public record SpawnPoint(String world, double x, double y, double z) {

    public static SpawnPoint fromLocation(Location location) {
        return new SpawnPoint(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    public Location toLocation() {
        World bukkitWorld = Bukkit.getWorld(world);
        return new Location(bukkitWorld, x, y, z);
    }

    public double horizontalDistance(Location other) {
        double dx = x - other.getX();
        double dz = z - other.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
