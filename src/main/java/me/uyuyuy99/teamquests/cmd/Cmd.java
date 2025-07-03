package me.uyuyuy99.teamquests.cmd;

import me.uyuyuy99.teamquests.TeamQuests;
import me.uyuyuy99.teamquests.quest.QuestManager;
import me.uyuyuy99.teamquests.team.TeamManager;

public abstract class Cmd {

    protected TeamQuests plugin;
    protected TeamManager teams;
    protected QuestManager quests;

    public Cmd() {
        this.plugin = TeamQuests.get();
        this.teams = plugin.teams();
        this.quests = plugin.quests();
    }

    public abstract void register(); // Register the command

}
