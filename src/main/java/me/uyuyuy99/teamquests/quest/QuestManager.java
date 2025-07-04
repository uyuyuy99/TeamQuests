package me.uyuyuy99.teamquests.quest;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.uyuyuy99.teamquests.Config;
import me.uyuyuy99.teamquests.TeamQuests;
import me.uyuyuy99.teamquests.team.Team;
import me.uyuyuy99.teamquests.util.CC;
import me.uyuyuy99.teamquests.util.ItemUtil;
import me.uyuyuy99.teamquests.util.NumberUtil;
import me.uyuyuy99.teamquests.util.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestManager {

    private TeamQuests plugin;
    private File questsConfigFile;
    private List<Quest> questList = new ArrayList<>();

    public QuestManager() {
        this.plugin = TeamQuests.get();
        this.questsConfigFile = new File(plugin.getDataFolder(), "quests.yml");

        plugin.getServer().getPluginManager().registerEvents(new QuestListeners(this), plugin);
        load();
    }

    // Returns quest with specified ID, or NULL if there is none
    public Quest getQuest(String id) {
        for (Quest quest : questList) {
            if (quest.getId().equals(id)) {
                return quest;
            }
        }
        return null;
    }

    // Add progress to a team quest by quest type/object
    public void progress(Player player, QuestType type, long progress, Object object) {
        Team team = plugin.teams().getTeam(player);
        if (team == null) return; // Can't do quests if not part of a team

        for (Quest quest : questList) {
            if (team.hasCompleted(quest)) continue; // Don't bother progressing completed quests
            if (quest.getType() != type || !quest.isValidObject(object)) continue;

            // Add the progress & check if they finished
            team.addQuestProgress(quest, progress);
            checkCompletion(team, quest);
        }
    }
    public void progress(Player player, QuestType type, long progress) {
        progress(player, type, progress, null);
    }
    public void progress(Player player, QuestType type, Object object) {
        progress(player, type, 1, object);
    }
    public void progress(Player player, QuestType type) {
        progress(player, type, 1, null);
    }

    // Give progress to a specific quest (used for custom quests). Returns true if progress was added
    public boolean progress(Player player, Quest quest, long progress) {
        Team team = plugin.teams().getTeam(player);
        if (team == null) return false; // Can't do quests if not part of a team
        if (team.hasCompleted(quest)) return false; // Don't bother progressing completed quests

        // Add the progress & check if they finished
        team.addQuestProgress(quest, progress);
        checkCompletion(team, quest);
        return true;
    }

    private void checkCompletion(Team team, Quest quest) {
        if (team.hasCompleted(quest)) {
            quest.givePrize(team);
            plugin.getLogger().info("Team '" + CC.strip(team.getName()) + "' (ID: " + team.getId()
                    + ") completed quest '" + CC.strip(quest.getName()) + "' (ID: " + quest.getId() + ")");
        }
    }

    // Returns true if player has unfinished quests of the given type
    public boolean hasUnfinishedQuest(Team team, QuestType type) {
        for (Team.Progress progress : team.getQuestProgresses()) {
            Quest quest = getQuest(progress.getQuestId());
            if (quest != null && quest.getType() == type && !progress.hasCompleted()) {
                return true;
            }
        }
        return false;
    }

    // Loads the quest list from quests.yml
    public void load() {
        questList.clear();

        // Save default yml file
        if (!questsConfigFile.exists()) {
            plugin.saveResource(questsConfigFile.getName(), false);
        }

        // Reload configs
        FileConfiguration questConfig = YamlConfiguration.loadConfiguration(questsConfigFile);

        // Look for defaults in the jar
        Reader defConfigStream1 = new InputStreamReader(plugin.getResource(questsConfigFile.getName()), StandardCharsets.UTF_8);
        if (defConfigStream1 != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream1);
            questConfig.setDefaults(defConfig);
        }

        // Add the quests
        for (String key : questConfig.getKeys(false)) {
            ConfigurationSection section = questConfig.getConfigurationSection(key);
            Quest quest = new Quest(section.getName(), QuestType.valueOf(section.getString("type").toUpperCase()));

            quest.setName(CC.translate(section.getString("name")));
            quest.setGoal(section.getLong("goal"));
            quest.setIcon(ItemUtil.getIconFromConfig(section, "icon"));
            if (section.isSet("valid-objects")) quest.setValidObjects(section.getStringList("valid-objects"));
            if (section.isSet("prize-commands")) quest.setPrizeCmds(section.getStringList("prize-commands"));

            questList.add(quest);
        }
    }

    // Returns false if player has no team; opens menu and returns true if they do
    public boolean openGui(Player player) {
        Team team = plugin.teams().getTeam(player);
        if (team == null) return false;

        // Create the layout for the GUI
        int rows = Config.get().getInt("quest-gui.rows");
        String[] layout = new String[rows];
        Arrays.fill(layout, "QQQQQQQQQ");

        // Add borders of filler icons if needed
        if (Config.get().getBoolean("quest-gui.vertical-border")) {
            for (int i = 0; i < rows; i++) {
                String line = layout[i];
                StringBuilder newLine = new StringBuilder(line);
                newLine.setCharAt(0, ' ');
                newLine.setCharAt(8, ' ');
                layout[i] = newLine.toString();
            }
        }
        if (Config.get().getBoolean("quest-gui.horizontal-border")) {
            layout[0] = "         ";
            layout[rows - 1] = "         ";
        }

        // Create the GUI & filler icon
        InventoryGui gui = new InventoryGui(plugin,
                Config.getString("quest-gui.title"),
                layout);
        ItemStack fillerIcon = ItemUtil.getIconFromConfig(Config.get(), "quest-gui.border-icon");
        ItemUtil.setItemName(fillerIcon, " ");
        gui.setFiller(fillerIcon);

        // Add all the quests to the GUI
        GuiElementGroup group = new GuiElementGroup('Q');
        for (Quest quest : questList) {
            // Get the quest progress info for this team
            Team.Progress progress = team.getQuestProgress(quest);
            long progressAmt = progress.getProgress();
            if (quest.getType() == QuestType.TRAVEL_BLOCKS) progressAmt /= 1000; // Distance is measured in millimeteres
            progressAmt = Math.min(progressAmt, quest.getGoal()); // Don't show any progress past the goal

            // Build the progress bar
            int percentComplete = (int) (((double) progressAmt) / ((double) quest.getGoal()) * 100.0);
            StringBuilder progressBar = new StringBuilder(progressAmt >= quest.getGoal()
                    ? ChatColor.GREEN.toString()
                    : ChatColor.YELLOW.toString());
            for (int i = 0; i < 10; i++) {
                if (i == percentComplete / 10) {
                    progressBar.append(ChatColor.DARK_GRAY);
                }
                progressBar.append("■");
            }
            progressBar.append(" ");
            progressBar.append(ChatColor.AQUA).append(NumberUtil.formatLong(progressAmt)).append(ChatColor.GRAY)
                    .append("/").append(ChatColor.YELLOW).append(NumberUtil.formatLong(quest.getGoal()));
            progressBar.append(" ").append(ChatColor.DARK_GRAY).append("(").append(percentComplete).append("%)");

            // Create the quest icon lore & add to the element group
            List<String> lore = Config.getStringList(
                    progress.hasCompleted() ? "quest-gui.quest-finished-text" : "quest-gui.quest-unfinished-text",
                    "quest", quest.getName(),
                    "progressbar", progressBar,
                    "completed", TimeUtil.formatDate(progress.getCompletedAt()),
                    "cooldown", TimeUtil.formatDate(progress.getCooldownEnd())
            );
            group.addElement(new StaticGuiElement('q',
                    quest.getIcon(),
                    lore.toArray(new String[]{}))
            );
        }
        gui.addElement(group);

        gui.show(player);
        return true;
    }

}
