package me.uyuyuy99.teamquests.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class QuestListeners implements Listener {

    private QuestManager quests;

    public QuestListeners(QuestManager quests) {
        this.quests = quests;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        //TODO execute command queue
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        quests.progress(event.getPlayer(), QuestType.PLACE_BLOCKS, event.getBlockPlaced().getType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        quests.progress(event.getPlayer(), QuestType.BREAK_BLOCKS, event.getBlock().getType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpGained(PlayerExpChangeEvent event) {
        if (event.getAmount() > 0) {
            quests.progress(event.getPlayer(), QuestType.EARN_EXP, event.getAmount());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer != null) {
            quests.progress(killer, QuestType.KILL_ENTITIES, event.getEntity().getType());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        long distance = (long) (Math.abs(event.getFrom().distance(event.getTo())) * 1000);
        quests.progress(event.getPlayer(), QuestType.TRAVEL_BLOCKS, distance);
    }

}
