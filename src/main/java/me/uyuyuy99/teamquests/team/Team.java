package me.uyuyuy99.teamquests.team;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import dev.dejvokep.boostedyaml.serialization.standard.TypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.uyuyuy99.teamquests.Config;
import me.uyuyuy99.teamquests.TeamQuests;
import me.uyuyuy99.teamquests.quest.Quest;
import me.uyuyuy99.teamquests.quest.QuestType;
import me.uyuyuy99.teamquests.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Member;
import java.util.*;

@SuppressWarnings("unchecked")
@Getter
public class Team {

    private final int id;
    private final YamlDocument storage;

    private final String name;
    private final UUID leader;
    private final List<Member> members;
    private final List<Progress> questProgresses;
    private final List<Invite> invites;

    // Create a new team
    public Team(int id, Player leader, String name) {
        this.id = id;
        this.storage = getYaml(id);
        this.leader = leader.getUniqueId();
        this.name = name;
        this.members = new ArrayList<>();
        this.questProgresses = new ArrayList<>();
        this.invites = new ArrayList<>();
        addMember(leader); // this sets dirty = true, so new team will be saved
    }

    // Load an existing team from disk
    public Team(int id) {
        this.id = id;
        storage = getYaml(id);
        name = storage.getString("name");
        leader = UUID.fromString(storage.getString("leader"));
        members = (List<Member>) storage.getList("members");
        questProgresses = (List<Progress>) storage.getList("questProgress");
        invites = (List<Invite>) storage.getList("invites");

        // Remove progress of quests that are no longer available
        questProgresses.removeIf(prog -> TeamQuests.get().quests().getQuest(prog.getQuestId()) == null);
    }

    // Gets existing or creates new yml file for team with given ID
    private YamlDocument getYaml(int id) {
        try {
            return YamlDocument.create(new File(TeamManager.getFolder(), id + ".yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public void save() {
        storage.set("name", name);
        storage.set("leader", leader.toString());
        storage.set("members", members);
        storage.set("questProgress", questProgresses);
        storage.set("invites", invites);
        storage.save();
    }

    // Gets member object corresponding to the given player on this team, or null if none exists
    private Member getMember(UUID uuid) {
        for (Member member : members) {
            if (member.getUuid().equals(uuid)) {
                return member;
            }
        }
        return null;
    }
    public Member getMember(Player player) {
        return getMember(player.getUniqueId());
    }

    // Adds a member to the team
    public void addMember(Player player) {
        members.add(new Member(player));
        invites.removeIf(invite -> invite.getUuid().equals(player.getUniqueId())); // Remove invite
    }

    public boolean isLeader(Player player) {
        return player.getUniqueId().equals(leader);
    }

    // Get the cached name of the leader
    public String getLeaderName() {
        return getMember(leader).getName();
    }

    // Sends a message to all online team members
    public void sendMessage(String msg) {
        for (Member member : members) {
            Player pl = Bukkit.getPlayer(member.getUuid());
            if (pl != null) pl.sendMessage(msg);
        }
    }

    public void addToCommandQueue(Member member, String cmd) {
        member.getCmdQueue().add(cmd);
    }

    // Runs all the quest prize commands that hadn't been executed for this player yet, returns true if any were run
    public boolean executeCommandQueue(Player player) {
        Member member = getMember(player);
        if (member == null || member.getCmdQueue().isEmpty()) return false;

        for (String cmd : member.getCmdQueue()) {
            Bukkit.getServer().dispatchCommand(
                    Bukkit.getServer().getConsoleSender(),
                    cmd.replace("{player}", player.getName())
            );
        }
        member.getCmdQueue().clear();
        return true;
    }

    public Progress getQuestProgress(Quest quest) {
        // Find progress object
        Progress progress = null;
        for (Progress p : questProgresses) {
            if (p.getQuestId().equals(quest.getId())) {
                progress = p;
                break;
            }
        }

        // If none exists, make a new one
        if (progress == null) {
            progress = new Progress(quest.getId(), 0L, 0L);
            questProgresses.add(progress);
        }

        return progress;
    }

    // Checks if team has reached the goal for a specific quest
    public boolean hasCompleted(Quest quest) {
        Progress progress = getQuestProgress(quest);
        long progressAmt = progress.getProgress();
        if (quest.getType() == QuestType.TRAVEL_BLOCKS) progressAmt /= 1000; // Distance is measured in millimeteres

        if (progressAmt >= quest.getGoal()) {
            progress.setCompletedAt(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public void addQuestProgress(Quest quest, long toAdd) {
        getQuestProgress(quest).addProgress(toAdd);
    }

    public boolean isInvited(Player player) {
        for (Invite invite : invites) {
            if (invite.getUuid().equals(player.getUniqueId()) && !invite.isExpired()) {
                return true;
            }
        }
        return false;
    }

    // Invites a player to the team
    public void addInvite(Player player) {
        invites.add(new Invite(player));
    }

    // Removes player from this team
    public void removePlayer(Player player) {
        members.removeIf(member -> member.getUuid().equals(player.getUniqueId()));
    }

    // Used to store data for each member of the team
    @Getter @AllArgsConstructor
    public static class Member {

        private UUID uuid;
        @Setter
        private String name;
        private long joinedAt; // Timestamp when player joined team
        private List<String> cmdQueue; // Prize commands yet to be run

        public Member(Player player) {
            this.uuid = player.getUniqueId();
            this.name = player.getName();
            this.joinedAt = System.currentTimeMillis();
            this.cmdQueue = new ArrayList<>();
        }

        public Player getPlayer() {
            return Bukkit.getPlayer(uuid);
        }

        // Returns true if the "/team leave" cooldown has expired
        public boolean canLeaveTeam() {
            return (System.currentTimeMillis() - joinedAt) > (Config.get().getInt("options.team-leave-cooldown") * 60000L);
        }

        // Gets the amount of time left before the "/team leave" cooldown expires
        public long getLeaveCooldownLeft() {
            return (Config.get().getInt("options.team-leave-cooldown") * 60000L) - (System.currentTimeMillis() - joinedAt);
        }

    }

    // Used to store progress/cooldown for quests
    @Getter @Setter @AllArgsConstructor
    public static class Progress {

        private final String questId;
        private long progress; // Current quest progress. Default 0
        private long completedAt; // Timestamp for completion date, used for cooldown. Default 0

        public void addProgress(long toAdd) {
            progress += toAdd;
        }

        public boolean hasCompleted() {
            return completedAt > 0;
        }

        public boolean isCooldownOver() {
            return (System.currentTimeMillis() - completedAt) > (Config.get().getInt("options.quest-cooldown") * 60000L);
        }

        // Gets the epoch ms time of the end of the cooldown
        public long getCooldownEnd() {
            return completedAt + (Config.get().getInt("options.quest-cooldown") * 60000L);
        }

    }

    @Getter @Setter @AllArgsConstructor
    public static class Invite {

        private final UUID uuid;
        private long sentAt;

        public Invite(Player player) {
            this.uuid = player.getUniqueId();
            this.sentAt = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return (System.currentTimeMillis() - sentAt) > (Config.get().getInt("options.invites-expire-after") * 60000L);
        }

    }

    // Register type adapters for serializing/deserializing custom objects for yaml
    static {
        TypeAdapter<Member> memberAdapter = new TypeAdapter<>() {
            @NotNull
            @Override
            public Map<Object, Object> serialize(@NotNull Member member) {
                Map<Object, Object> map = new HashMap<>();
                map.put("uuid", member.getUuid().toString());
                map.put("name", member.getName());
                map.put("joinedAt", member.getJoinedAt());
                map.put("cmdQueue", member.getCmdQueue());
                return map;
            }

            @NotNull
            @Override
            public Member deserialize(@NotNull Map<Object, Object> map) {
                return new Member(
                        UUID.fromString((String) map.get("uuid")),
                        (String) map.get("name"),
                        NumberUtil.longValue(map.get("joinedAt")),
                        (List<String>) map.get("cmdQueue")
                );
            }
        };

        TypeAdapter<Progress> progressAdapter = new TypeAdapter<>() {
            @NotNull
            @Override
            public Map<Object, Object> serialize(@NotNull Progress progress) {
                Map<Object, Object> map = new HashMap<>();
                map.put("questId", progress.getQuestId());
                map.put("progress", progress.getProgress());
                map.put("completedAt", progress.getCompletedAt());
                return map;
            }

            @NotNull
            @Override
            public Progress deserialize(@NotNull Map<Object, Object> map) {
                return new Progress(
                        (String) map.get("questId"),
                        NumberUtil.longValue(map.get("progress")),
                        NumberUtil.longValue(map.get("completedAt"))
                );
            }
        };

        TypeAdapter<Invite> inviteAdapter = new TypeAdapter<>() {
            @NotNull
            @Override
            public Map<Object, Object> serialize(@NotNull Invite invite) {
                Map<Object, Object> map = new HashMap<>();
                map.put("uuid", invite.getUuid().toString());
                map.put("sentAt", invite.getSentAt());
                return map;
            }

            @NotNull
            @Override
            public Invite deserialize(@NotNull Map<Object, Object> map) {
                return new Invite(
                        UUID.fromString((String) map.get("uuid")),
                        NumberUtil.longValue(map.get("sentAt"))
                );
            }
        };

        StandardSerializer.getDefault().register(Member.class, memberAdapter);
        StandardSerializer.getDefault().register(Progress.class, progressAdapter);
        StandardSerializer.getDefault().register(Invite.class, inviteAdapter);
    }

}
