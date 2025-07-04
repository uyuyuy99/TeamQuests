package me.uyuyuy99.teamquests.cmd;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import me.uyuyuy99.teamquests.Config;
import me.uyuyuy99.teamquests.team.Team;
import me.uyuyuy99.teamquests.util.TimeUtil;

import java.util.Optional;

public class TeamQuestsCmd extends SubCmd {

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("quests")
                .withPermission("teamquests.quests")
                .withAliases("quest")
                .executesPlayer((player, args) -> {
                    if (!quests.openGui(player)) {
                        Config.sendMsg("not-in-team", player);
                    }
                });
    }

}
