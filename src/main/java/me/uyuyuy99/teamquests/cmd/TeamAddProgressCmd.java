package me.uyuyuy99.teamquests.cmd;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LongArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import me.uyuyuy99.teamquests.Config;
import me.uyuyuy99.teamquests.quest.Quest;
import me.uyuyuy99.teamquests.team.Team;
import me.uyuyuy99.teamquests.util.NumberUtil;
import org.bukkit.entity.Player;

public class TeamAddProgressCmd extends SubCmd {

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("addprogress")
                .withPermission("teamquests.admin.addprogress")
                .withArguments(new PlayerArgument("player"), new StringArgument("quest"))
                .withOptionalArguments(new LongArgument("progress"))
                .executes((sender, args) -> {
                    Player player = (Player) args.get("player");
                    String questId = (String) args.get("quest");
                    Quest quest = quests.getQuest(questId);

                    // Check if quest with given ID exists
                    if (quest == null) {
                        Config.sendMsg("no-quest-found", sender, "questid", questId);
                        return;
                    }

                    // Add quest progress to the player's team
                    long toAdd = NumberUtil.longValue(args.getOptional("progress").orElse(1L));
                    if (quests.progress(player, quest, toAdd)) {
                        Team team = teams.getTeam(player);
                        Config.sendMsg("add-quest-progress", sender,
                                "progress", toAdd,
                                "quest", quest.getName(),
                                "team", team.getName(),
                                "teamid", team.getId()
                        );
                    }
                });
    }

}
