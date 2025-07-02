package me.uyuyuy99.teamquests.quest;

import me.uyuyuy99.teamquests.TeamQuests;
import me.uyuyuy99.teamquests.team.Team;
import me.uyuyuy99.teamquests.util.CC;
import me.uyuyuy99.teamquests.util.ItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    // Give progress to a specific quest (used for custom quests)
    public void progress(Player player, Quest quest, long progress) {
        Team team = plugin.teams().getTeam(player);
        if (team == null) return; // Can't do quests if not part of a team
        if (team.hasCompleted(quest)) return; // Don't bother progressing completed quests

        // Add the progress & check if they finished
        team.addQuestProgress(quest, progress);
        checkCompletion(team, quest);
    }

    private void checkCompletion(Team team, Quest quest) {
        if (team.hasCompleted(quest)) {
            quest.givePrize(team);
        }
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

            if (quest.getType() == QuestType.TRAVEL_BLOCKS) {
                quest.setGoal(quest.getGoal() * 1000);
            }

            questList.add(quest);
        }
    }

    // Returns false if player has no team; opens menu and returns true if they do
//    public boolean openGui(Player player) {
//        Team team = plugin.teams().getTeam(player);
//        if (team == null) return false;
//
//        List<String> layout = Config.get().getStringList("quest-gui.layout");
//        ListIterator<String> iter = layout.listIterator();
//        char slotChar = 'B';
//        while (iter.hasNext()) {
//            String line = iter.next();
//
//            while (line.contains("A")) {
//                line = line.replaceFirst("A", String.valueOf(slotChar++));
//            }
//            iter.set(line);
//        }
//
//        InventoryGui gui = new InventoryGui(plugin,
//                Config.get().getString("quest-gui.title")
//                        .replace("%time%", Util.getReadableTime(nextReset - System.currentTimeMillis())),
//                layout.toArray(new String[0]));
//        gui.setFiller(Util.getIconFromConfig("quest-gui.filler-icon"));
//
//        slotChar = 'B';
//
//        for (Quest quest : activeQuests) {
//            StringBuilder progressBar = new StringBuilder(CC.GREEN);
//
//            if (playerData.hasCompleted(quest)) {
//                progressBar.append(CC.BOLD).append("Completed!");
//            } else {
//                int progressBarLength = 30;
//                int progress = (int) (((float) playerData.getQuestProgress(quest)) / ((float) quest.getGoal()) * progressBarLength);
//
//                for (int j=0; j<progressBarLength; j++) {
//                    if (j == progress)
//                        progressBar.append(CC.DARK_GRAY);
//                    progressBar.append("|");
//                }
//            }
//
//            List<String> text = Config.get().getStringList("quest-gui.item-text");
//            text.replaceAll(s -> s.replace("%quest%", quest.getName()));
//            text.replaceAll(s -> s.replace("%progressbar%", progressBar.toString()));
//            text = CC.format(text);
//
//            gui.addElement(new StaticGuiElement(slotChar++,
//                    quest.getIcon(),
//                    text.toArray(new String[0])
//            ));
//        }
//
//        gui.show(player);
//        return true;
//    }

}
