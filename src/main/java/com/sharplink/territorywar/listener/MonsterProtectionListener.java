package com.sharplink.territorywar.listener;

import com.sharplink.territorywar.territory.TerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

/** 팀 또는 서버가 소유한 영토 안에서는 몬스터가 스폰되거나 머무를 수 없다. */
public final class MonsterProtectionListener implements Listener {

    private final TerritoryManager territoryManager;
    private final String worldName;

    public MonsterProtectionListener(TerritoryManager territoryManager, String worldName) {
        this.territoryManager = territoryManager;
        this.worldName = worldName;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Monster && territoryManager.isClaimed(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    /** 소환/이동 등 스폰 이벤트를 거치지 않고 영토 안에 들어온 몬스터를 주기적으로 청소한다. */
    public void startPeriodicPurge(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return;
            }
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity instanceof Monster && territoryManager.isClaimed(entity.getLocation())) {
                    entity.remove();
                }
            }
        }, 100L, 100L);
    }
}
