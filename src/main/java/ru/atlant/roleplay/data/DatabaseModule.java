package ru.atlant.roleplay.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.data.factory.AsyncQueryFactory;
import ru.atlant.roleplay.data.factory.SyncQueryFactory;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleFactory;
import ru.atlant.roleplay.util.ExecutorUtil;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class DatabaseModule implements Module {

    private static final Consumer<Throwable> CATCHER = Throwable::printStackTrace;

    private final RolePlay rolePlay;
    private final ModuleFactory factory;

    private SyncQueryFactory sync;
    private AsyncQueryFactory async;

    @Override
    public void onEnable() {
        FileConfiguration mainConfig = rolePlay.getConfig();
        String host = mainConfig.getString("data.host"), user = mainConfig.getString("data.user"), database = mainConfig.getString("data.database"), password = mainConfig.getString("data.password");
        int port = mainConfig.getInt("data.port");
        HikariConfig config = new HikariConfig();
        config.setConnectionTimeout(5000);
        config.setMaximumPoolSize(10);
        config.setAutoCommit(true);
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s" +
                "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", host, port, database));
        config.setUsername(user);
        config.setPassword(password);
        HikariDataSource dataSource = new HikariDataSource(config);

        this.sync = new SyncQueryFactory(dataSource, CATCHER);
        this.async = new AsyncQueryFactory(dataSource, CATCHER, ExecutorUtil.EXECUTOR);
    }

    public SyncQueryFactory sync() {
        return sync;
    }

    public AsyncQueryFactory async() {
        return async;
    }

}
