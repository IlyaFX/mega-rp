package ru.atlant.roleplay.prison;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.config.ConfigModule;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.user.UsersModule;
import ru.atlant.roleplay.util.Colors;
import ru.atlant.roleplay.util.DataUtil;
import ru.atlant.roleplay.util.FormatUtil;
import ru.atlant.roleplay.util.WorldUtil;

import java.util.Objects;

@RequiredArgsConstructor
@LoadAfter(clazz = {UsersModule.class, ConfigModule.class})
public class PrisonModule implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    private Location prisonLocation;
    private Location unarrestLocation;

    private double maxPrisonDistance;

    @Override
    public void onEnable() {
        UsersModule users = registry.get(UsersModule.class);
        ConfigModule config = registry.get(ConfigModule.class);
        config
                .subscribe("prison-location", loc -> prisonLocation = loc, DataUtil::locationFromString, new Location(Bukkit.getWorlds().get(0), 0, 120, 0), true)
                .subscribe("unarrest-location", loc -> unarrestLocation = loc, DataUtil::locationFromString, new Location(Bukkit.getWorlds().get(0), 100, 120, 0), true)
                .subscribe("max-prison-distance", dis -> maxPrisonDistance = dis, Double::parseDouble, 30.0, true);
        ;
        users.subscribe((old, data) -> {
            Player player = Bukkit.getPlayer(data.getUser());
            if (old == null)
                return;
            if (old.getStars() != data.getStars()) {
                player.sendTitle(FormatUtil.starsFormat(data.getStars()), Colors.cWhite + "Новый уровень розыска!", 5, 50, 5);
            }
            if (old.getPrison() > 0 && data.getPrison() <= 0) {
                unarrest(player);
            }
            if (old.getPrison() <= 0 && data.getPrison() > 0) {
                arrest(player);
            }
        }, true);
        Bukkit.getScheduler().runTaskTimer(rolePlay, () -> {
            Bukkit.getOnlinePlayers().stream().map(player -> users.dataUnsafe(player.getUniqueId())).filter(Objects::nonNull).filter(UsersModule.UserData::inPrison).forEach(data -> {
                users.replaceViolation(data.getUser(), data.getStars(), data.getPrison() - 1000);
            });
        }, 20, 20);
        Bukkit.getScheduler().runTaskTimer(rolePlay, () -> {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player ->
                            users.data(player.getUniqueId())
                                    .map(UsersModule.UserData::inPrison)
                                    .orElse(false) && WorldUtil.distance(player, prisonLocation) > maxPrisonDistance)
                    .forEach(this::arrestTeleport);
        }, 40, 40);
    }

    public void arrest(Player player) {
        arrestTeleport(player);
    }

    public void arrestTeleport(Player player) {
        player.teleport(prisonLocation);
    }

    public void unarrest(Player player) {
        unarrestTeleport(player);
    }

    public void unarrestTeleport(Player player) {
        player.teleport(unarrestLocation);
    }
}
