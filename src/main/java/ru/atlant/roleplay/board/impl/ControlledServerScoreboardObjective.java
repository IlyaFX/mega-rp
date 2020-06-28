package ru.atlant.roleplay.board.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class ControlledServerScoreboardObjective implements IServerScoreboardObjective {

    @Setter
    @Getter
    private String displayName;
    @Getter
    private final Object2IntMap<String> scores = Object2IntMaps.synchronize(new Object2IntOpenHashMap<>());

    @Override
    public String getId() {
        return "server";
    }

    @Override
    public boolean hasScore(String name) {
        return scores.containsKey(name);
    }

    @Override
    public Integer getScore(String name) {
        return scores.get(name);
    }

    @Override
    public void setScore(String name, int value) {
        scores.put(name, value);
    }

    @Override
    public boolean removeScore(String name) {
        return scores.remove(name) != null;
    }

    @Override
    public void clearScores() {
        scores.clear();
    }

    @Override
    public void applyScores(Object2IntMap<String> scores) {
        this.scores.clear();
        this.scores.putAll(scores);
    }

    @Override
    public void applyLines(String... lines) {
        applyLines(Arrays.asList(lines));
    }

    @Override
    public void applyLines(Collection<String> lines) {
        int i = lines.size();
        Object2IntMap<String> scores = new Object2IntOpenHashMap<>(i);
        val iterator = lines.iterator();
        for (int x = i; x-- >= 0; ) {
            scores.put(iterator.next(), x);
        }
        applyScores(scores);
    }

    @Override
    public void subscribe(UUID player) {
    }

    @Override
    public void unsubscribe(UUID player) {
    }

    @Override
    public void unsubscribe(UUID player, boolean fast) {
    }

    @Override
    public DisplaySlot getDisplaySlot() {
        return null;
    }

    @Override
    public void setDisplaySlot(DisplaySlot displaySlot) {
    }
}
