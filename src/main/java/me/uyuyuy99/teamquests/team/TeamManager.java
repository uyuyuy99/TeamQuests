package me.uyuyuy99.teamquests.team;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.SneakyThrows;
import me.uyuyuy99.teamquests.Config;
import me.uyuyuy99.teamquests.TeamQuests;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class TeamManager implements Listener {

    private TeamQuests plugin;

    private static File teamsFolder;
    private final YamlDocument playerStorage;
    private int nextTeamId;
    private final Map<UUID, Integer> teamIdMap = new HashMap<>(); // Contains IDs of all players (light on memory)
    private final Map<Integer, Team> teamMap = new HashMap<>(); // Only loads teams into memory when needed

    public TeamManager() throws IOException {
        this.plugin = TeamQuests.get();
        teamsFolder = new File(TeamQuests.get().getDataFolder(), "teams");
        //noinspection ResultOfMethodCallIgnored
        teamsFolder.mkdirs();

        playerStorage = YamlDocument.create(
                new File(teamsFolder, "_players.yml"),
                plugin.getResource("_players.yml")
        );
        nextTeamId = playerStorage.getInt("nextTeamId");
        Section teamSection = playerStorage.getSection("teams");
        for (Map.Entry<String, Object> entry : teamSection.getStringRouteMappedValues(false).entrySet()) {
            teamIdMap.put(UUID.fromString(entry.getKey()), (int) entry.getValue());
        }

        // Clear expired invites
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Team team : teamMap.values()) {
                    Iterator<Team.Invite> iter = team.getInvites().iterator();

                    while (iter.hasNext()) {
                        Team.Invite invite = iter.next();

                        if (invite.isExpired()) {
                            Player invitedPlayer = Bukkit.getPlayer(invite.getUuid());
                            Config.sendMsg("invite-expired", invitedPlayer, "team", team.getName());
                            iter.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        // Reset completed quests after cooldown
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Team team : teamMap.values()) {
                    for (Team.Progress prog : team.getQuestProgresses()) {
                        if (prog.hasCompleted() && prog.isCooldownOver()) {
                            prog.setProgress(0);
                            prog.setCompletedAt(0);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 200L);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Save _players.yml
    @SneakyThrows
    private void savePlayers() {
        Section teamSection = playerStorage.getSection("teams");
        if (teamSection != null) teamSection.clear();

        for (Map.Entry<UUID, Integer> entry : teamIdMap.entrySet()) {
            teamSection.set(entry.getKey().toString(), entry.getValue());
        }
        playerStorage.save();
    }

    // Save _players.yml and all the team yml's
    public void saveAll() {
        savePlayers();
        teamMap.values().forEach(Team::save);
    }

    // Load the player's Team object from the corresponding yml file if it's not already in memory
    public void loadPlayerIfNeeded(Player player) {
        Integer teamId = teamIdMap.get(player.getUniqueId());

        if (teamId != null && !teamMap.containsKey(teamId)) {
            teamMap.put(teamId, new Team(teamId));
        }
    }

    // Load player's team on join, and execute any prize commands still in queue
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerIfNeeded(player);

        Team team = getTeam(player);
        if (team != null && team.executeCommandQueue(player)) {
            plugin.getLogger().info("Executed prize commands in queue for player: " + player.getName());
        }
    }

    // Returns null if player has no team, otherwise returns their team
    public Team getTeam(Player player) {
        Integer teamId = teamIdMap.get(player.getUniqueId());

        if (teamId != null) {
            return teamMap.get(teamId);
        }
        return null;
    }

    // Creates & returns a new team
    public Team createTeam(Player leader, String name) {
        int teamId = nextTeamId++;
        Team team = new Team(teamId, leader, name);
        teamIdMap.put(leader.getUniqueId(), teamId);
        teamMap.put(teamId, team);
        return team;
    }

    public static File getFolder() {
        return teamsFolder;
    }

}
