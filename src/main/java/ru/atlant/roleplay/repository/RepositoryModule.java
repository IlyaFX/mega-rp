package ru.atlant.roleplay.repository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.repository.impl.RolePlayDataRepository;

@RequiredArgsConstructor
public class RepositoryModule implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    @Getter
    private RolePlayDataRepository repository;

    @Override
    public void onEnable() {
        this.repository = new RolePlayDataRepository(rolePlay, rolePlay.getConfig().getString("repository"));
        this.repository.init();
    }
}
