package ru.atlant.roleplay.prison;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.config.ConfigModule;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.user.UsersModule;
import ru.atlant.roleplay.util.DataUtil;

@RequiredArgsConstructor
@LoadAfter(clazz = {UsersModule.class, ConfigModule.class})
public class PrisonModule implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    private Location prisonLocation;

    @Override
    public void onEnable() {
        UsersModule users = registry.get(UsersModule.class);
        ConfigModule config = registry.get(ConfigModule.class);
        config
                .subscribe("prison-location", loc -> prisonLocation = loc, DataUtil::locationFromString, new Location(Bukkit.getWorlds().get(0), 0, 120, 0), true);
        users.subscribe(data -> {
            // check for prison
        }, true);
        // scheduler (distance to prison and teleport))
    }
}
