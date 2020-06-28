package ru.atlant.roleplay.board.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.IScoreboardCriteria;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.*;

public class ControlledPlayerScoreboardObjective implements IServerScoreboardObjective {
    private static final int MAX_SCORE_LENGTH = 64;

    @Getter
    private final ControlledPlayerScoreboard scoreboard;
    private final String id;
    private final Map<String, Integer> scores = new HashMap<>();

    private final Set<UUID> subscribed = Collections.synchronizedSet(new HashSet<>());
    private String displayName;
    private DisplaySlot displaySlot;


    public ControlledPlayerScoreboardObjective(ControlledPlayerScoreboard scoreboard, String id, String displayName, DisplaySlot displaySlot, boolean autoSubscribe) {
        this.scoreboard = scoreboard;
        this.id = id;
        this.displayName = displayName;
        this.displaySlot = displaySlot;
    }

    public ControlledPlayerScoreboardObjective(ControlledPlayerScoreboard scoreboard, String id, String displayName, DisplaySlot displaySlot) {
        this(scoreboard, id, displayName, displaySlot, true);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        if (this.displayName.equals(displayName)) {
            return;
        }

        this.displayName = displayName;
        scoreboard.broadcastPacket(subscribed, newObjectivePacket(2));
    }

    @Override
    public DisplaySlot getDisplaySlot() {
        return this.displaySlot;
    }

    private int displaySlotToId(DisplaySlot displaySlot) {
        switch (displaySlot) {
            case BELOW_NAME:
                return 2;
            case PLAYER_LIST:
                return 0;
            case SIDEBAR:
                return 1;
        }
        return -1;
    }

    @Override
    public void setDisplaySlot(DisplaySlot displaySlot) {
        if (this.displaySlot == displaySlot) {
            return;
        }

        this.displaySlot = displaySlot;
        scoreboard.broadcastPacket(subscribed, newDisplaySlotPacket(displaySlotToId(displaySlot)));
    }

    @Override
    public Map<String, Integer> getScores() {
        return scores;
    }

    @Override
    public boolean hasScore(String name) {
        return scores.containsKey(name);
    }

    @Override
    public Integer getScore(String name) {
        return scores.getOrDefault(name, 0);
    }

    @Override
    public void setScore(String name, int value) {
        if (name.length() > MAX_SCORE_LENGTH)
            name = name.substring(0, MAX_SCORE_LENGTH);

        if (!scores.containsKey(name))
            scores.put(name, 0);

        int oldValue = scores.put(name, value);
        if (oldValue == value) {
            return;
        }

        scoreboard.broadcastPacket(subscribed, newScorePacket(name, value, 0));
    }

    @Override
    public boolean removeScore(String name) {
        if (name.length() > MAX_SCORE_LENGTH)
            name = name.substring(0, MAX_SCORE_LENGTH);
        if (scores.remove(name) == null) {
            return false;
        }

        scoreboard.broadcastPacket(subscribed, newScorePacket(name, 0, 1));
        return true;
    }

    @Override
    public void clearScores() {
        scores.clear();

        scoreboard.broadcastPacket(subscribed, newObjectivePacket(1));
        for (UUID player : subscribed) {
            subscribe(player);
        }
    }

    @Override
    public void applyScores(Object2IntMap<String> scores) {
        Set<String> toRemove = new HashSet<>(getScores().keySet());
        for (val score : scores.keySet()) {
            if (score.length() > MAX_SCORE_LENGTH)
                toRemove.remove(score.substring(0, MAX_SCORE_LENGTH));
            else
                toRemove.remove(score);
        }
        for (String name : toRemove) {
            removeScore(name);
        }
        for (val score : scores.object2IntEntrySet()) {
            setScore(score.getKey(), score.getIntValue());
        }
    }

    @Override
    public void applyLines(String... lines) {
        applyLines(Arrays.asList(lines));
    }

    @Override
    public void applyLines(Collection<String> lines) {
        Object2IntMap<String> scores = new Object2IntOpenHashMap<>();
        int i = lines.size();
        for (String line : lines) {
            scores.put(line, i--);
        }
        applyScores(scores);
    }

    @Override
    public void subscribe(UUID player) {
        scoreboard.sendPacket(newObjectivePacket(0), player);
        scoreboard.sendPacket(newDisplaySlotPacket(displaySlotToId(getDisplaySlot())), player);
        for (Map.Entry<String, Integer> score : getScores().entrySet()) {
            scoreboard.sendPacket(newScorePacket(score.getKey(), score.getValue(), 0), player);
        }
        subscribed.add(player);
    }

    @Override
    public void unsubscribe(UUID player) {
        unsubscribe(player, false);
    }

    @Override
    public void unsubscribe(UUID player, boolean fast) {
        if (!this.subscribed.remove(player) || fast) {
            return;
        }

        this.scoreboard.sendPacket(newObjectivePacket(1), player);
    }

    void unsubscribeAll() {
        this.scoreboard.broadcastPacket(this.subscribed, newObjectivePacket(1));
        this.subscribed.clear();
    }

    private Object newObjectivePacket(int mode) {
        val packet = new PacketPlayOutScoreboardObjective();
        packet.a = id;
        packet.b = displayName;
        packet.c = IScoreboardCriteria.EnumScoreboardHealthDisplay.values()[0];
        packet.d = mode;
        return packet;
    }

    private Object newScorePacket(String name, int value, int action) {
        val packet = new PacketPlayOutScoreboardScore();
        packet.a = name;
        packet.b = id;
        packet.c = value;
        packet.d = PacketPlayOutScoreboardScore.EnumScoreboardAction.values()[action];
        return packet;
    }

    private Object newDisplaySlotPacket(int slot) {
        val packet = new PacketPlayOutScoreboardDisplayObjective();
        packet.a = slot;
        packet.b = id;
        return packet;
    }
}
