package com.sharplink.territorywar.command;

import com.sharplink.territorywar.gui.FlagListMenu;
import com.sharplink.territorywar.model.PlayerRecord;
import com.sharplink.territorywar.model.Team;
import com.sharplink.territorywar.team.TeamManager;
import com.sharplink.territorywar.territory.CellCoord;
import com.sharplink.territorywar.territory.TerritoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TeamCommand implements CommandExecutor, TabCompleter {

    private final TeamManager teamManager;
    private final TerritoryManager territoryManager;

    public TeamCommand(TeamManager teamManager, TerritoryManager territoryManager) {
        this.teamManager = teamManager;
        this.territoryManager = territoryManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("플레이어만 사용할 수 있는 명령어입니다.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            return showInfo(player);
        }

        if (args[0].equalsIgnoreCase("join")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("사용법: /team join <팀 이름>", NamedTextColor.RED));
                return true;
            }
            return joinBloc(player, args[1]);
        }

        if (args[0].equalsIgnoreCase("flags")) {
            return showFlags(player);
        }

        if (args[0].equalsIgnoreCase("displace")) {
            return forceDisplace(player, args);
        }

        player.sendMessage(Component.text("사용법: /team [info|join|flags|displace] <팀 이름>", NamedTextColor.RED));
        return true;
    }

    private boolean forceDisplace(Player sender, String[] args) {
        if (!sender.hasPermission("territorywar.admin")) {
            sender.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("사용법: /team displace <플레이어1> [플레이어2] ...", NamedTextColor.RED));
            return true;
        }

        List<UUID> targets = new ArrayList<>();
        List<String> notFound = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            Player target = Bukkit.getPlayerExact(args[i]);
            if (target == null) {
                notFound.add(args[i]);
            } else {
                targets.add(target.getUniqueId());
            }
        }
        if (!notFound.isEmpty()) {
            sender.sendMessage(Component.text("온라인 상태의 플레이어를 찾을 수 없습니다: " + String.join(", ", notFound), NamedTextColor.RED));
            return true;
        }

        teamManager.forceDisplace(targets);
        sender.sendMessage(Component.text(targets.size() + "명을 반편입자 무리로 강제 전환했습니다. (테스트용)", NamedTextColor.GREEN));
        return true;
    }

    private boolean showFlags(Player player) {
        PlayerRecord record = teamManager.getOrCreatePlayerRecord(player.getUniqueId());
        if (record.getTeamId() == null) {
            player.sendMessage(Component.text("소속 팀이 없어 깃발 목록을 볼 수 없습니다.", NamedTextColor.RED));
            return true;
        }

        Team team = teamManager.getTeam(record.getTeamId());
        List<CellCoord> cells = territoryManager.getCellsOwnedBy(team.getId());
        if (cells.isEmpty()) {
            player.sendMessage(Component.text("아직 보유한 영토가 없습니다.", NamedTextColor.YELLOW));
            return true;
        }

        FlagListMenu.open(player, team.getName(), territoryManager, cells);
        return true;
    }

    private boolean showInfo(Player player) {
        PlayerRecord record = teamManager.getOrCreatePlayerRecord(player.getUniqueId());

        if (record.getTeamId() == null && !record.isDisplaced()) {
            player.sendMessage(Component.text("아직 어떤 팀에도 속해있지 않습니다. 깃발을 만들어 설치하면 팀을 창단할 수 있습니다.", NamedTextColor.YELLOW));
            return true;
        }

        if (record.isDisplaced()) {
            int blocSize = teamManager.getBlocMembers(record.getDisplacedBlocId()).size();
            player.sendMessage(Component.text("현재 무소속 반편입자입니다. 같은 무리(" + blocSize + "명)가 함께 뭉쳐야 다른 팀에 합류할 수 있습니다.", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("/team join <팀 이름> 으로 무리 전체를 이동시킬 수 있습니다.", NamedTextColor.YELLOW));
            return true;
        }

        Team team = teamManager.getTeam(record.getTeamId());
        long cells = territoryManager.countCells(team.getId());
        player.sendMessage(Component.text("소속 팀: " + team.getName(), NamedTextColor.AQUA));
        player.sendMessage(Component.text("역할: " + record.getRole(), NamedTextColor.AQUA));
        player.sendMessage(Component.text("팀원 수: " + team.getMembers().size() + "명", NamedTextColor.AQUA));
        player.sendMessage(Component.text("보유 영토: " + cells + "칸", NamedTextColor.AQUA));
        return true;
    }

    private boolean joinBloc(Player player, String teamName) {
        PlayerRecord record = teamManager.getOrCreatePlayerRecord(player.getUniqueId());

        if (!record.isDisplaced()) {
            player.sendMessage(Component.text("무소속 반편입자만 사용할 수 있는 명령어입니다.", NamedTextColor.RED));
            return true;
        }

        Team target = teamManager.getTeamByName(teamName);
        if (target == null) {
            player.sendMessage(Component.text("존재하지 않는 팀입니다: " + teamName, NamedTextColor.RED));
            return true;
        }

        UUID blocId = record.getDisplacedBlocId();
        int blocSize = teamManager.getBlocMembers(blocId).size();
        teamManager.joinBloc(blocId, target);

        player.getServer().broadcast(Component.text(
                "반편입자 무리(" + blocSize + "명)가 \"" + target.getName() + "\" 팀에 합류했습니다.",
                NamedTextColor.GOLD));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("info", "join", "flags", "displace").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            return teamManager.getActiveTeams().stream().map(Team::getName).toList();
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("displace")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
