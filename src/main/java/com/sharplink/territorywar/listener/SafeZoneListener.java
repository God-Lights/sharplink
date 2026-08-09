package com.sharplink.territorywar.listener;

import com.sharplink.territorywar.territory.TerritoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/** 서버 소유 영토(안전지대) 안에서는 플레이어 간 전투를 막는다. */
public final class SafeZoneListener implements Listener {

    private final TerritoryManager territoryManager;

    public SafeZoneListener(TerritoryManager territoryManager) {
        this.territoryManager = territoryManager;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) {
            return;
        }
        if (territoryManager.isServerTerritory(victim.getLocation())) {
            event.setCancelled(true);
            attacker.sendMessage(Component.text("안전지대에서는 다른 플레이어를 공격할 수 없습니다.", NamedTextColor.RED));
        }
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }
}
