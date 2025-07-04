package me.uyuyuy99.teamquests;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import lombok.SneakyThrows;
import me.uyuyuy99.teamquests.cmd.TeamCmd;
import me.uyuyuy99.teamquests.quest.QuestManager;
import me.uyuyuy99.teamquests.team.TeamManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeamQuests extends JavaPlugin {

    private static TeamQuests plugin;
    private TeamManager teams;
    private QuestManager quests;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        plugin = this;
        //noinspection ResultOfMethodCallIgnored
        getDataFolder().mkdirs();

        Config.load();

        // Load data
        getLogger().info("Loading teams...");
        teams = new TeamManager();
        getLogger().info("Loading quests...");
        quests = new QuestManager();

        // Load player data in case anyone is online when plugin is loaded
        for (Player pl : getServer().getOnlinePlayers()) {
            teams.loadPlayerIfNeeded(pl);
        }

        // Register commands
        new TeamCmd().register();
    }

    @Override
    public void onDisable() {
        teams.saveAll();
    }

    public static TeamQuests get() {
        return plugin;
    }

    public TeamManager teams() {
        return teams;
    }

    public QuestManager quests() {
        return quests;
    }

}
