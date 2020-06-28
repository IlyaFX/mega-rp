package ru.atlant.roleplay.board;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.board.impl.ControlledPlayerScoreboard;
import ru.atlant.roleplay.board.impl.IServerScoreboardObjective;
import ru.atlant.roleplay.board.impl.SimpleBoardObjective;
import ru.atlant.roleplay.event.EventExecutorModule;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.repository.RepositoryModule;
import ru.atlant.roleplay.repository.impl.RolePlayDataRepository;
import ru.atlant.roleplay.util.ExecutorUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@LoadAfter(clazz = {RepositoryModule.class, EventExecutorModule.class})
public class ScoreBoardModule implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    private final Map<UUID, Map<String, SimpleBoardObjective>> objectiveMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> currentObjectiveMap = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<String>> history = new HashMap<>();
    private final ControlledPlayerScoreboard controlledPlayerScoreboard = new ControlledPlayerScoreboard();
    @Setter
    private long updateInterval = 100L;

    @Override
    public void onEnable() {
        RolePlayDataRepository repository = registry.get(RepositoryModule.class).getRepository();
        // TODO: listen to repository boards update
        val eventExecutor = registry.get(EventExecutorModule.class);
        eventExecutor.registerListener(PlayerQuitEvent.class, e -> {
            val uuid = e.getPlayer().getUniqueId();
            objectiveMap.remove(uuid);
            currentObjectiveMap.remove(uuid);
            history.remove(uuid);
        }, EventPriority.HIGH, false);
        val executorService = ExecutorUtil.EXECUTOR;
        executorService.submit(() ->
        {
            for (; ; ) {
                val startTime = System.currentTimeMillis();
                for (val s : objectiveMap.entrySet()) {
                    val objective = s.getValue().get(currentObjectiveMap.get(s.getKey()));
                    if (objective != null) objective.update();
                }
                val doneTime = System.currentTimeMillis() - startTime;
                if (doneTime < updateInterval) {
                    try {
                        Thread.sleep(updateInterval - doneTime);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
        });
    }

    public void setCurrentObjective(UUID player, String objective) {
        val deque = history.computeIfAbsent(player, key -> new ArrayDeque<>(5));
        val map = objectiveMap.computeIfAbsent(player, key -> new HashMap<>(4));
        if (objective != null && !map.containsKey(objective)) {
            return;
        }
        val previousName = currentObjectiveMap.get(player);
        if (Objects.equals(previousName, objective)) {
            return;
        }
        if (previousName != null) {
            val boardObjective = map.get(previousName);
            if (boardObjective.isStoreInHistory()) {
                deque.push(previousName);
            }
            boardObjective.getObjective().unsubscribe(player);
        }
        if (objective != null) {
            currentObjectiveMap.put(player, objective);
            map.get(objective).getObjective().subscribe(player);
        } else {
            currentObjectiveMap.remove(player);
        }
    }

    public String getCurrentObjective(UUID player) {
        return currentObjectiveMap.get(player);
    }

    public SimpleBoardObjective getPlayerObjective(UUID player, String objectiveName) {
        val objectives = objectiveMap.computeIfAbsent(player, key -> new HashMap<>(4));
        return objectives.computeIfAbsent(objectiveName, k -> {
            IServerScoreboardObjective controlledScoreboardObjective = controlledPlayerScoreboard.createPlayerObjective(
                    player, k, DisplaySlot.SIDEBAR, false
            );
            return new SimpleBoardObjective(controlledScoreboardObjective);
        });
    }
}
