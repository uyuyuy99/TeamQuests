package me.uyuyuy99.teamquests.cmd;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import me.uyuyuy99.teamquests.Config;
import me.uyuyuy99.teamquests.team.Team;

import java.util.stream.Collectors;

public class TeamCmd extends Cmd {

    @Override
    public void register() {
        CommandAPI.unregister("team");
        new CommandAPICommand("team")
                .withSubcommand(new TeamCreateCmd().build())
                .withSubcommand(new TeamInviteCmd().build())
                .withSubcommand(new TeamJoinCmd().build())
                .withSubcommand(new TeamLeaveCmd().build())
                .withSubcommand(new TeamQuestsCmd().build())
                .withSubcommand(new TeamAddProgressCmd().build())
                .withPermission("teamquests.info")
                .executesPlayer((player, args) -> {
                    Team team = teams.getTeam(player);

                    // Check if player is not in team
                    if (team == null) {
                        Config.sendMsg("not-in-team", player);
                        return;
                    }

                    // Send player team info message
                    String memberString = team.getMembers().stream()
                            .map(Team.Member::getName)
                            .collect(Collectors.joining(", "));
                    Config.getStringList("messages.team-info",
                            "player", player.getName(),
                            "team", team.getName(),
                            "leader", team.getLeaderName(),
                            "members", memberString
                    ).forEach(player::sendMessage);
                })
                .register();
    }

}
