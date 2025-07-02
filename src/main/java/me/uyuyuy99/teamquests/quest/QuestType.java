package me.uyuyuy99.teamquests.quest;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

@Getter
public enum QuestType {

    EARN_EXP,
    REACH_LEVEL,
    PLACE_BLOCKS(Material.class),
    BREAK_BLOCKS(Material.class),
    TRAVEL_BLOCKS,
    KILL_ENTITIES(EntityType.class),
    CUSTOM,
    ;

    private final Class<?> validObjectType;

    QuestType(Class<?> optionType) {
        this.validObjectType = optionType;
    }
    QuestType() {
        this(null);
    }

}
