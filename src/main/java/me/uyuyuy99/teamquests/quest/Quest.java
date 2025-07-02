package me.uyuyuy99.teamquests.quest;

import lombok.Getter;
import lombok.Setter;
import me.uyuyuy99.teamquests.Config;
import me.uyuyuy99.teamquests.team.Team;
import me.uyuyuy99.teamquests.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Quest {

    private final String id;

    private QuestType type;
    private String name;
    private long goal;
    private ItemStack icon;
    private List<Object> validObjects = new ArrayList<>(); // The entities/blocks that count toward the goal
    private List<String> prizeCmds = new ArrayList<>(); // Commands that execute for each player when completed

    public Quest(String id, QuestType type) {
        this.id = id;
        this.type = type;
    }

    public void setValidObjects(List<String> objectNames) {
        validObjects.clear();

        for (String s : objectNames) {
            if (type.getValidObjectType().equals(Material.class)) {
                validObjects.add(Material.valueOf(s.toUpperCase()));
            }
            else if (type.getValidObjectType().equals(EntityType.class)) {
                validObjects.add(EntityType.valueOf(s.toUpperCase()));
            }
            else {
                validObjects.add(s);
            }
        }
    }

    public boolean isValidObject(Object object) {
        if (validObjects.isEmpty() || object == null) return true;
        if (object.getClass() == type.getValidObjectType() && validObjects.contains(object)) return true;

        return false;
    }

    // Execute all prize commands for each player in team, or add to their command queue if offline
    public void givePrize(Team team) {
        for (Team.Member member : team.getMembers()) {
            for (String cmd : prizeCmds) {
                team.addToCommandQueue(member, cmd); // Queue each command
            }
            Player pl = member.getPlayer();
            if (pl != null) {
                team.executeCommandQueue(pl); // If player online, execute commands now
                Config.sendMsg("completed-quest", pl,
                        "team", team.getName(),
                        "quest", name
                );
            }
        }
    }

}
