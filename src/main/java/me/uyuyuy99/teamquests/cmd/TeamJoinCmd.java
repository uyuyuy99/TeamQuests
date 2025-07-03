package me.uyuyuy99.teamquests.cmd;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import me.uyuyuy99.teamquests.Config;
import me.uyuyuy99.teamquests.team.Team;

import java.util.Optional;

public class TeamJoinCmd extends SubCmd {

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("join")
                .withPermission("teamquests.join")
                .withArguments(new GreedyStringArgument("team"))
                .executesPlayer((player, args) -> {
                    String teamName = (String) args.get("team");

                    if (teams.getTeam(player) == null) {
                        Optional<Team> optTeam = teams.getTeams().stream()
                                .filter(t -> t.getName().toLowerCase().startsWith(teamName.toLowerCase()) && t.isInvited(player))
                                .findFirst();

                        if (optTeam.isPresent()) {
                            Team team = optTeam.get();

                            // Check if team has reached max capacity
                            int maxSize = Config.get().getInt("options.max-team-size");
                            if (team.getMembers().size() >= maxSize) {
                                Config.sendMsg("team-reached-player-limit", player,
                                        "team", team.getName(),
                                        "max", maxSize
                                );
                                return;
                            }

                            team.sendMessage(Config.getMsg("joined-your-team", "player", player.getName()));
                            team.addMember(player);
                            Config.sendMsg("joined-another-team", player, "team", team.getName());
                        } else {
                            Config.sendMsg("no-active-invites", player, "team", teamName);
                        }
                    } else {
                        Config.sendMsg("cant-join-while-in-team", player);
                    }
                });
    }

}
