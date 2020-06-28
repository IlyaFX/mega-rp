package ru.atlant.roleplay.board.impl;

import org.bukkit.scoreboard.DisplaySlot;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IServerScoreboard<O extends IServerScoreboardObjective> {
    O createObjective(String id, String title, DisplaySlot displaySlot, boolean autoSubscribe);

    default O createObjective(String id, String title, DisplaySlot displaySlot) {
        return createObjective(id, title, displaySlot, true);
    }

    default O createObjective(String title, DisplaySlot displaySlot, boolean autoSubscribe) {
        return createObjective(Long.toHexString(System.nanoTime()), title, displaySlot, autoSubscribe);
    }

    default O createObjective(String title, DisplaySlot displaySlot) {
        return createObjective(title, displaySlot, true);
    }

    @Nullable
    O getObjective(String id);

    boolean removeObjective(String id);

    O createPlayerObjective(UUID player, String id, String title, DisplaySlot displaySlot, boolean autoSubscribe);

    default O createPlayerObjective(UUID player, String id, String title, DisplaySlot displaySlot) {
        return createPlayerObjective(player, id, title, displaySlot, true);
    }

    default O createPlayerObjective(UUID player, String title, DisplaySlot displaySlot, boolean autoSubscribe) {
        return createPlayerObjective(player, Long.toHexString(System.nanoTime()), title, displaySlot, autoSubscribe);
    }

    default O createPlayerObjective(UUID player, String title, DisplaySlot displaySlot) {
        return createPlayerObjective(player, title, displaySlot, true);
    }

    @Nullable
    O getPlayerObjective(UUID player, String id);

    boolean removePlayerObjective(UUID player, String id);

}
