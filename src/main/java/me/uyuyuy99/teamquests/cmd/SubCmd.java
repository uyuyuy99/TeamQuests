package me.uyuyuy99.teamquests.cmd;

import dev.jorel.commandapi.CommandAPICommand;
import me.uyuyuy99.teamquests.TeamQuests;
import me.uyuyuy99.teamquests.quest.QuestManager;
import me.uyuyuy99.teamquests.team.TeamManager;

public abstract class SubCmd {

    protected TeamQuests plugin;
    protected TeamManager teams;
    protected QuestManager quests;

    public SubCmd() {
        this.plugin = TeamQuests.get();
        this.teams = plugin.teams();
        this.quests = plugin.quests();
    }

    public abstract CommandAPICommand build();

}
