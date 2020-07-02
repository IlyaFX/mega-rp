package ru.atlant.roleplay.repository.impl;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import ru.atlant.roleplay.repository.AbstractWebRepository;

import java.util.concurrent.TimeUnit;

@Getter
public class RolePlayDataRepository extends AbstractWebRepository<RolePlayData> {

    public RolePlayDataRepository(Plugin plugin, String url) {
        super(plugin, url, TimeUnit.SECONDS, 30L);
    }

    @Override
    public Class<RolePlayData> getClazz() {
        return RolePlayData.class;
    }
}
