package com.sharplink.territorywar.gui;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public final class FlagListMenuHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, Location> targets = new HashMap<>();

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setTarget(int slot, Location location) {
        targets.put(slot, location);
    }

    public Location getTarget(int slot) {
        return targets.get(slot);
    }
}
