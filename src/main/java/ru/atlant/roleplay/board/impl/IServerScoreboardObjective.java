package ru.atlant.roleplay.board.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface IServerScoreboardObjective {

    String getId();

    String getDisplayName();

    void setDisplayName(String displayName);

    Map<String, Integer> getScores();

    boolean hasScore(String name);

    Integer getScore(String name);

    void setScore(String name, int value);

    boolean removeScore(String name);

    void clearScores();

    void applyScores(Object2IntMap<String> scores);

    void applyLines(String... lines);

    void subscribe(UUID v);

    void unsubscribe(UUID v);

    void applyLines(Collection<String> lines);

    void unsubscribe(UUID player, boolean fast);

    DisplaySlot getDisplaySlot();

    void setDisplaySlot(DisplaySlot displaySlot);

}
