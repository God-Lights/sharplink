package com.sharplink.territorywar.command;

import com.sharplink.territorywar.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class GameCommand implements CommandExecutor, TabCompleter {

    private final GameManager gameManager;

    public GameCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("territorywar.admin")) {
            sender.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text("사용법: /game [start|reset|debug] ...", NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (gameManager.start()) {
                    Bukkit.broadcast(Component.text("영토전쟁 게임이 시작되었습니다!", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("이미 시작했거나 종료된 게임입니다. 다시 시작하려면 /game reset을 먼저 사용하세요.", NamedTextColor.RED));
                }
            }
            case "reset" -> {
                gameManager.reset();
                Bukkit.broadcast(Component.text("영토전쟁 게임이 초기화되었습니다. /game start로 새 게임을 시작할 수 있습니다.", NamedTextColor.GOLD));
            }
            case "debug" -> {
                if (args.length < 2 || (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {
                    sender.sendMessage(Component.text("사용법: /game debug <on|off>", NamedTextColor.RED));
                    return true;
                }
                boolean enable = args[1].equalsIgnoreCase("on");
                gameManager.setDebugMode(enable);
                sender.sendMessage(Component.text("디버그(테스트) 모드: " + (enable ? "켜짐" : "꺼짐"), NamedTextColor.YELLOW));
            }
            default -> sender.sendMessage(Component.text("사용법: /game [start|reset|debug] ...", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("start", "reset", "debug").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            return List.of("on", "off").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
