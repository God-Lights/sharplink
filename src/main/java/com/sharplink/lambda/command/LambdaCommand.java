package com.sharplink.lambda.command;

import com.sharplink.lambda.LambdaPlugin;
import com.sharplink.lambda.economy.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class LambdaCommand implements CommandExecutor, TabCompleter {

    private final LambdaPlugin plugin;
    private final EconomyManager economyManager;

    public LambdaCommand(LambdaPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return sendOwnBalance(sender);
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("balance")) {
            if (args.length >= 2) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                sender.sendMessage(Component.text(target.getName() + "님의 잔액: "
                        + economyManager.getCurrencySettings().format(economyManager.getBalance(target.getUniqueId())),
                        NamedTextColor.AQUA));
                return true;
            }
            return sendOwnBalance(sender);
        }

        if (sub.equals("give") || sub.equals("set")) {
            if (!sender.hasPermission("lambda.admin")) {
                sender.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(Component.text("사용법: /lambda " + sub + " <플레이어> <금액>", NamedTextColor.RED));
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(Component.text("해당 플레이어를 찾을 수 없습니다.", NamedTextColor.RED));
                return true;
            }

            long amount;
            try {
                amount = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("금액은 숫자여야 합니다.", NamedTextColor.RED));
                return true;
            }

            if (!economyManager.hasAccount(target.getUniqueId())) {
                economyManager.createAccount(target.getUniqueId(), 0);
            }

            if (sub.equals("give")) {
                economyManager.deposit(target.getUniqueId(), amount);
            } else {
                economyManager.setBalance(target.getUniqueId(), amount);
            }

            sender.sendMessage(Component.text(target.getName() + "님의 잔액이 "
                    + economyManager.getCurrencySettings().format(economyManager.getBalance(target.getUniqueId()))
                    + "(으)로 변경되었습니다.", NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(Component.text("사용법: /lambda [balance|give|set] <플레이어> <금액>", NamedTextColor.RED));
        return true;
    }

    private boolean sendOwnBalance(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("사용법: /lambda balance <플레이어>", NamedTextColor.RED));
            return true;
        }
        long balance = economyManager.getBalance(player.getUniqueId());
        sender.sendMessage(Component.text("현재 잔액: " + economyManager.getCurrencySettings().format(balance), NamedTextColor.AQUA));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("balance", "give", "set").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return names;
        }
        return List.of();
    }
}
