package com.sharplink.territorywar.item;

import com.sharplink.territorywar.TerritoryWarPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * 서버 소유 중립 영토를 표시하는 깃발. 제작법이 없고 관리자 명령어로만 지급된다.
 */
public final class ServerFlagItem {

    private final NamespacedKey key;

    public ServerFlagItem(TerritoryWarPlugin plugin) {
        this.key = new NamespacedKey(plugin, "server_flag");
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.WHITE_BANNER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("서버 깃발", NamedTextColor.AQUA, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("설치하면 그 자리가 서버 영토이자 서버 스폰이 됩니다.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("관리자만 설치/파괴할 수 있습니다.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isServerFlag(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
