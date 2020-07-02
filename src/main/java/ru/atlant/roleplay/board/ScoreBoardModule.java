package ru.atlant.roleplay.board;

import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.board.impl.ControlledPlayerScoreboard;
import ru.atlant.roleplay.board.impl.IRecordData;
import ru.atlant.roleplay.board.impl.IServerScoreboardObjective;
import ru.atlant.roleplay.board.impl.SimpleBoardObjective;
import ru.atlant.roleplay.board.provider.PlaceholderProvider;
import ru.atlant.roleplay.board.provider.PlaceholdersAPIPlaceholderProvider;
import ru.atlant.roleplay.board.provider.expansion.SimplePlaceholderAPIExpansion;
import ru.atlant.roleplay.event.EventExecutorModule;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.repository.RepositoryModule;
import ru.atlant.roleplay.repository.impl.RolePlayData;
import ru.atlant.roleplay.repository.impl.RolePlayDataRepository;
import ru.atlant.roleplay.user.UsersModule;
import ru.atlant.roleplay.util.ExecutorUtil;
import ru.atlant.roleplay.util.FormatUtil;
import ru.atlant.roleplay.util.TimeUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@LoadAfter(clazz = {RepositoryModule.class, EventExecutorModule.class, UsersModule.class})
public class ScoreBoardModule implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    private final Map<UUID, Map<String, SimpleBoardObjective>> objectiveMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> currentObjectiveMap = new ConcurrentHashMap<>();
    private final ControlledPlayerScoreboard controlledPlayerScoreboard = new ControlledPlayerScoreboard();
    @Setter
    private long updateInterval = 100L;

    private Map<String, Board> boardDataMap = new HashMap<>();

    private PlaceholderProvider placeholderProvider;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderProvider = new PlaceholdersAPIPlaceholderProvider();
            System.out.println("Use PlaceholderAPI as placeholders provider for scoreboards.");
        } else {
            System.out.println("ERROR! RolePlay plugin needs PlaceholderAPI. I had to insert a stub here, but........... SHUTDOWN!");
            Bukkit.shutdown();
            return;
        }
        UsersModule users = registry.get(UsersModule.class);
        Map<String, Function<Player, String>> placeholders = new HashMap<String, Function<Player, String>>() {{
            put("job", (player) -> users.data(player.getUniqueId()).map(data -> data.getJob().getName()).orElse("Не имеется"));
            put("fraction", (player) -> users.data(player.getUniqueId()).map(data -> data.getJob().getFraction().getName()).orElse("Не имеется"));
            put("arrest_time", (player) -> users.data(player.getUniqueId()).map(data -> TimeUtil.formatTime(data.getPrison(), true)).orElse("0 секунд"));
            put("stars", (player) -> users.data(player.getUniqueId()).map(data -> FormatUtil.starsFormat(data.getStars())).orElse(FormatUtil.starsFormat(0)));
        }};
        placeholders.forEach((identifier, function) -> new SimplePlaceholderAPIExpansion(identifier, function).register());
        RolePlayDataRepository repository = registry.get(RepositoryModule.class).getRepository();
        repository.subscribe(data -> {
            boardDataMap = data.getBoards().stream().map(this::wrap).collect(Collectors.toMap(
                    Board::getId,
                    board -> board
            ));
            Bukkit.getOnlinePlayers().forEach(player -> setCurrentObjective(player.getUniqueId(), null));
            objectiveMap.clear();
            Bukkit.getOnlinePlayers().forEach(player -> {
                fill(player);
                String val = getCurrentObjective(player.getUniqueId());
                if (val != null && boardDataMap.containsKey(val))
                    setCurrentObjective(player.getUniqueId(), val);
            });
        }, true);
        val eventExecutor = registry.get(EventExecutorModule.class);
        eventExecutor.registerListener(PlayerQuitEvent.class, e -> {
            val uuid = e.getPlayer().getUniqueId();
            objectiveMap.remove(uuid);
            currentObjectiveMap.remove(uuid);
        }, EventPriority.HIGH, false);
        eventExecutor.registerListener(PlayerJoinEvent.class, e -> fill(e.getPlayer()), EventPriority.HIGH, false);
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
        Bukkit.getScheduler().runTaskTimer(rolePlay, () -> {
            Bukkit.getOnlinePlayers().stream().map(player -> users.dataUnsafe(player.getUniqueId())).filter(Objects::nonNull).forEach(data -> {
                setCurrentObjective(data.getUser(), data.inPrison() ? "prison" : "game");
            });
        }, 10, 10);
    }

    private void fill(Player player) {
        boardDataMap.forEach((key, board) -> {
            SimpleBoardObjective objective = getPlayerObjective(player.getUniqueId(), key);
            board.fill(player, objective);
        });
    }

    private Board wrap(RolePlayData.BoardData boardData) {
        return new Board(boardData.getId(), boardData.getTitle(), (player) -> boardData.getLines().stream().map(line -> placeholderProvider.transform(player, line)).collect(Collectors.toList()));
    }

    public void setCurrentObjective(UUID player, String objective) {
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

    @AllArgsConstructor
    @Getter
    private static class Board {

        private final String id;
        private final String title;
        private final Function<Player, List<IRecordData>> record;

        public void fill(Player player, SimpleBoardObjective objective) {
            objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
            record.apply(player).forEach(objective::record);
        }

    }
}
