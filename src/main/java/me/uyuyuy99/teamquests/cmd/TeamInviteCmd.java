package me.uyuyuy99.teamquests.cmd;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.uyuyuy99.teamquests.Config;
import me.uyuyuy99.teamquests.team.Team;
import org.bukkit.entity.Player;

public class TeamInviteCmd extends SubCmd {

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("invite")
                .withPermission("teamquests.invite")
                .withArguments(new PlayerArgument("player"))
                .executesPlayer((player, args) -> {
                    Player invitee = (Player) args.get("player");
                    Team team = teams.getTeam(player);

                    if (team != null) {
                        // Check if inviter must be team leader
                        if (Config.get().getBoolean("options.only-leader-can-invite") && !team.isLeader(player)) {
                            Config.sendMsg("only-leader-can-invite", player, "leader", team.getLeaderName());
                            return;
                        }

                        // Check if team has reached max capacity
                        int maxSize = Config.get().getInt("options.max-team-size");
                        if (team.getMembers().size() >= maxSize) {
                            Config.sendMsg("team-reached-player-limit", player,
                                    "team", team.getName(),
                                    "max", maxSize
                            );
                            return;
                        }

                        // Check if player already has an active invite to this team
                        if (team.isInvited(invitee)) {
                            Config.sendMsg("already-invited", player, "player", invitee.getName());
                            return;
                        }

                        // Once all checks are done, send out the invite
                        team.addInvite(invitee);
                        Config.sendMsg("sent-invite", player,
                                "player", invitee.getName(),
                                "team", team.getName()
                        );
                        Config.sendMsg("received-invite", invitee, "team", team.getName());
                    } else {
                        Config.sendMsg("cant-invite-while-in-team", player, "player", invitee);
                    }
                });
    }

}
