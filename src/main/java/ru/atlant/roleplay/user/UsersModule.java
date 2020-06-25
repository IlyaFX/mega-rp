package ru.atlant.roleplay.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.data.DatabaseModule;
import ru.atlant.roleplay.data.QueryResult;
import ru.atlant.roleplay.event.EventExecutorModule;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.work.Fraction;
import ru.atlant.roleplay.work.Job;
import ru.atlant.roleplay.work.WorksModule;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@LoadAfter(clazz = {DatabaseModule.class, EventExecutorModule.class, WorksModule.class})
public class UsersModule implements Module {

    private static final String CREATE_USER_FRACTIONS_TABLE = "CREATE TABLE IF NOT EXISTS `user_fractions` (`user` varchar(36) NOT NULL, `fraction` varchar(50) NOT NULL, `job` varchar(50) NOT NULL, PRIMARY KEY(`user`))  ENGINE=InnoDB DEFAULT CHARSET=utf8;";
    private static final String CREATE_USER_VIOLATIONS_TABLE = "CREATE TABLE IF NOT EXISTS `user_violations` (`user` varchar(36) NOT NULL, `stars` int NOT NULL, `prison` long NOT NULL, PRIMARY KEY(`user`))  ENGINE=InnoDB DEFAULT CHARSET=utf8;";

    private static final String SELECT_USER_DATA = "SELECT `fraction`,`job`,`stars`,`prison` FROM `user_fractions` LEFT JOIN `user_violations` ON `user_violations`.`user` = `user_fractions`.`user` WHERE `user_fractions`.`user` = ?";

    private static final String REPLACE_USER_FRACTION = "REPLACE INTO `user_fractions` VALUES (?,?,?)";
    private static final String REPLACE_USER_VIOLATION = "REPLACE INTO `user_violations` VALUES (?,?,?)";

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    private DatabaseModule database;

    private final Map<UUID, UserData> usersMap = new ConcurrentHashMap<>(4);

    @Override
    public void onEnable() {
        this.database = registry.get(DatabaseModule.class);
        database.sync().unsafeUpdate(CREATE_USER_FRACTIONS_TABLE);
        database.sync().unsafeUpdate(CREATE_USER_VIOLATIONS_TABLE);
        EventExecutorModule events = registry.get(EventExecutorModule.class);
        WorksModule works = registry.get(WorksModule.class);
        events.registerListener(AsyncPlayerPreLoginEvent.class, event -> {
            UUID uid = event.getUniqueId();
            try {
                QueryResult res = database.sync().prepareGet(SELECT_USER_DATA, ps -> {
                    try {
                        ps.setString(1, uid.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                UserData data;
                if (res.isEmpty()) {
                    replaceFraction(uid, works.getDefaultFraction(), works.getDefaultJob());
                    replaceViolation(uid, 0, 0);
                    data = new UserData(works.getDefaultJob(), 0, 0);
                } else {
                    QueryResult.SQLSection section = res.all().stream().findFirst().get();
                    data = new UserData(works.find(section.lookupValue("fraction"), section.lookupValue("job")), section.lookupValue("stars"), Long.parseLong(section.lookupValue("prison")));
                }
                usersMap.put(uid, data);
            } catch (Exception ex) {
                ex.printStackTrace();
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                event.setKickMessage("Не удалось загрузить информацию о игроке. Зайдите чуть позже! :з");
            }
        }, EventPriority.MONITOR, true);
    }

    public void replaceViolation(UUID user, int stars, long prisonTimeStamp) {
        database.async().prepareUpdate(REPLACE_USER_VIOLATION, ps -> {
            try {
                ps.setString(1, user.toString());
                ps.setInt(2, stars);
                ps.setLong(3, prisonTimeStamp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void replaceFraction(UUID user, Fraction fraction, Job job) {
        database.async().prepareUpdate(REPLACE_USER_FRACTION, ps -> {
            try {
                ps.setString(1, user.toString());
                ps.setString(2, fraction.getId());
                ps.setString(3, job.getId());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @AllArgsConstructor
    @Getter
    public static class UserData {

        private Job job;
        private int stars;
        private long prison;

    }

}
