package com.sharplink.territorywar.item;

import com.sharplink.territorywar.TerritoryWarPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class FlagItem {

    private final NamespacedKey key;
    private final NamespacedKey recipeKey;

    public FlagItem(TerritoryWarPlugin plugin) {
        this.key = new NamespacedKey(plugin, "flag");
        this.recipeKey = new NamespacedKey(plugin, "flag_recipe");
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.LIME_BANNER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("영토 깃발", NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("빈 땅에 설치하면 영토를 주장합니다.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("팀이 없다면 이 자리에 새 팀을 창단합니다.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isFlag(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public boolean isFlagBlock(Block block) {
        BlockState state = block.getState();
        if (!(state instanceof TileState tileState)) {
            return false;
        }
        return tileState.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public ShapedRecipe createRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, create());
        recipe.shape("GDG", "GDG");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
}
