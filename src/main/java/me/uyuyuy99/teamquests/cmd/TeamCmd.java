package me.uyuyuy99.teamquests.cmd;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;

public class TeamCmd extends Cmd {

    @Override
    public void register() {
        CommandAPI.unregister("team");
        new CommandAPICommand("team")
                .withSubcommand(new TeamCreateCmd().build())
                .withSubcommand(new TeamInviteCmd().build())
                .withSubcommand(new TeamJoinCmd().build())
                .withSubcommand(new TeamLeaveCmd().build())
                .register();
    }

}
