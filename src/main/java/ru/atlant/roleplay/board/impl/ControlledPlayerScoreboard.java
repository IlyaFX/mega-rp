package ru.atlant.roleplay.board.impl;

import lombok.val;
import net.minecraft.server.v1_12_R1.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.event.EventExecutorModule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ControlledPlayerScoreboard implements IServerScoreboard<IServerScoreboardObjective> {

    // objectives shared by all players.
    private final Map<String, ControlledPlayerScoreboardObjective> objectives = Collections.synchronizedMap(new HashMap<>());
    // per-player teams & objectives.
    private final Map<UUID, Map<String, ControlledPlayerScoreboardObjective>> playerObjectives = Collections.synchronizedMap(new HashMap<>());

    public ControlledPlayerScoreboard() {
        RolePlay.getInstance().getRegistry().get(EventExecutorModule.class).registerListener(PlayerQuitEvent.class, event -> {
            val player = event.getPlayer().getUniqueId();
            objectives.values().forEach(o -> o.unsubscribe(player, true));
            val playerObjectives = this.playerObjectives.remove(player);
            if (playerObjectives != null) {
                playerObjectives.values().forEach(o -> o.unsubscribe(player, true));
            }
        }, EventPriority.HIGHEST, false);
    }

    @Override
    public ControlledPlayerScoreboardObjective createObjective(String id, String title, DisplaySlot displaySlot, boolean autoSubscribe) {
        ControlledPlayerScoreboardObjective objective = new ControlledPlayerScoreboardObjective(this, id, title, displaySlot, autoSubscribe);
        this.objectives.put(id, objective);
        return objective;
    }

    @Override
    public ControlledPlayerScoreboardObjective getObjective(String id) {
        return this.objectives.get(id);
    }

    @Override
    public boolean removeObjective(String id) {
        val objective = objectives.remove(id);
        if (objective == null) {
            return false;
        }
        objective.unsubscribeAll();
        return true;
    }

    @Override
    public ControlledPlayerScoreboardObjective createPlayerObjective(UUID player, String id, String title, DisplaySlot displaySlot, boolean autoSubscribe) {
        val objectives = playerObjectives.computeIfAbsent(player, p -> new HashMap<>(2));

        val objective = new ControlledPlayerScoreboardObjective(this, id, title, displaySlot, autoSubscribe);
        if (autoSubscribe) {
            objective.subscribe(player);
        }
        objectives.put(id, objective);

        return objective;
    }

    @Override
    public ControlledPlayerScoreboardObjective getPlayerObjective(UUID player, String id) {
        val map = playerObjectives.get(player);
        if (map == null) {
            return null;
        }
        return map.get(id);
    }

    @Override
    public boolean removePlayerObjective(UUID player, String id) {
        val map = playerObjectives.get(player);
        if (map == null) {
            return false;
        }
        val objective = map.remove(id);
        if (objective == null) {
            return false;
        }
        objective.unsubscribeAll();
        return true;
    }

    void sendPacket(Object packet, UUID player) {
        val cp = Bukkit.getPlayer(player);
        if (cp != null)
            ((CraftPlayer) cp).getHandle().playerConnection.sendPacket((Packet) packet);
    }

    void broadcastPacket(Iterable<UUID> players, Object packet) {
        for (UUID player : players) {
            val p = Bukkit.getPlayer(player);
            if (p != null)
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket((Packet) packet);
        }
    }

}
