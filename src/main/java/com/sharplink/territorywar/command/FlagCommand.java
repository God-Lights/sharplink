package com.sharplink.territorywar.command;

import com.sharplink.territorywar.item.FlagItem;
import com.sharplink.territorywar.item.ServerFlagItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class FlagCommand implements CommandExecutor, TabCompleter {

    private final FlagItem flagItem;
    private final ServerFlagItem serverFlagItem;

    public FlagCommand(FlagItem flagItem, ServerFlagItem serverFlagItem) {
        this.flagItem = flagItem;
        this.serverFlagItem = serverFlagItem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("territorywar.admin")) {
            sender.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0 || (!args[0].equalsIgnoreCase("give") && !args[0].equalsIgnoreCase("serverflag"))) {
            sender.sendMessage(Component.text("사용법: /flag [give|serverflag] <플레이어>", NamedTextColor.RED));
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("온라인 상태의 플레이어를 찾을 수 없습니다: " + args[1], NamedTextColor.RED));
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(Component.text("콘솔에서는 대상 플레이어를 지정해야 합니다.", NamedTextColor.RED));
            return true;
        }

        ItemStack item = args[0].equalsIgnoreCase("serverflag") ? serverFlagItem.create() : flagItem.create();
        target.getInventory().addItem(item);
        sender.sendMessage(Component.text(target.getName() + "님에게 지급했습니다.", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("give", "serverflag").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
