package me.uyuyuy99.teamquests.cmd;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import me.uyuyuy99.teamquests.Config;
import me.uyuyuy99.teamquests.team.Team;
import me.uyuyuy99.teamquests.util.TimeUtil;

import java.util.Optional;

public class TeamLeaveCmd extends SubCmd {

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("leave")
                .withPermission("teamquests.leave")
                .withOptionalArguments(new StringArgument("confirm"))
                .executesPlayer((player, args) -> {
                    Team team = teams.getTeam(player);

                    // Check if player is not in a team
                    if (team == null) {
                        Config.sendMsg("not-in-team", player);
                        return;
                    }

                    // Check if team leave cooldown is still active
                    Team.Member member = team.getMember(player);
                    if (!member.canLeaveTeam()) {
                        Config.sendMsg("cant-leave-team", player,
                                "team", team.getName(),
                                "time", TimeUtil.formatTimeAbbr(member.getLeaveCooldownLeft() / 1000)
                        );
                        return;
                    }

                    // Once checks are passed, leave/disband the team depending on if player is the leader
                    if (team.isLeader(player)) {
                        // Only disband if user types: /team leave confirm
                        Optional<Object> confirm = args.getOptional("confirm");
                        if (confirm.orElse("").toString().equalsIgnoreCase("confirm")) {
                            team.sendMessage(Config.getMsg("disband", "team", team.getName()));
                            teams.disbandTeam(team);
                        } else {
                            Config.sendMsg("disband-confirm", player, "team", team.getName());
                        }
                    } else {
                        // Leave the team
                        teams.leave(player, team);
                        Config.sendMsg("team-leave", player, "team", team.getName());
                    }
                });
    }

}
