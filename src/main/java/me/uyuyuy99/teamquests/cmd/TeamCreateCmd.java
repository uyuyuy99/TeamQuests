package me.uyuyuy99.teamquests.cmd;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import me.uyuyuy99.teamquests.Config;

public class TeamCreateCmd extends SubCmd {

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("create")
                .withPermission("teamquests.create")
                .withArguments(new GreedyStringArgument("name"))
                .executesPlayer((player, args) -> {
                    String name = (String) args.get("name");

                    if (teams.getTeam(player) == null) {
                        int maxLength = Config.get().getInt("options.max-team-name-length");

                        if (name.length() <= maxLength) {
                            teams.createTeam(player, name);
                            Config.sendMsg("team-create", player, "team", name);
                        } else {
                            Config.sendMsg("team-name-too-long", player, "max", maxLength);
                        }
                    } else {
                        Config.sendMsg("cant-create-while-in-team", player);
                    }
                });
    }

}
