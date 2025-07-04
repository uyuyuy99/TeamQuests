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
import java.util.*;

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
        Section teamSection = playerStorage.createSection("teams");
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
        playerStorage.set("nextTeamId", nextTeamId);

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

        if (teamId != null) {
            if (!teamMap.containsKey(teamId)) {
                teamMap.put(teamId, new Team(teamId));
            }
            // Also update the player's name in case it changed
            teamMap.get(teamId).getMember(player).setName(player.getName());
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

    // Returns list of all teams currently loaded in memory
    public Collection<Team> getTeams() {
        return teamMap.values();
    }

    // Creates & returns a new team
    public void createTeam(Player leader, String name) {
        int teamId = nextTeamId++;
        Team team = new Team(teamId, leader, name);
        teamIdMap.put(leader.getUniqueId(), teamId);
        teamMap.put(teamId, team);
    }

    // Adds a player to a team
    public void join(Player player, Team team) {
        team.addMember(player);
        teamIdMap.put(player.getUniqueId(), team.getId());
    }

    // Removes a player from a team
    public void leave(Player player, Team team) {
        teamIdMap.remove(player.getUniqueId());
        team.removePlayer(player);
    }

    // Disbands a team (deletes it)
    public void disbandTeam(Team team) {
        File teamFile = team.getStorage().getFile();
        teamMap.remove(team.getId());
        teamIdMap.entrySet().removeIf(entry -> entry.getValue() == team.getId());

        // Delete team file on another thread
        new BukkitRunnable() {
            @Override
            public void run() {
                if (teamFile.delete()) {
                    plugin.getLogger().info("Successfully deleted team file: " + teamFile.getName());
                } else {
                    plugin.getLogger().warning("Unable to delete team file: " + teamFile.getName());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public static File getFolder() {
        return teamsFolder;
    }

}
