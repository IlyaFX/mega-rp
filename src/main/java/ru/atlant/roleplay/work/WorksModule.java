package ru.atlant.roleplay.work;

import lombok.RequiredArgsConstructor;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.repository.RepositoryModule;
import ru.atlant.roleplay.repository.impl.RolePlayData;
import ru.atlant.roleplay.repository.impl.RolePlayDataRepository;

@RequiredArgsConstructor
@LoadAfter(clazz = {RepositoryModule.class})
public class WorksModule implements Module {

	private final RolePlay rolePlay;
	private final ModuleRegistry registry;

	@Override
	public void onEnable() {
		RolePlayDataRepository repository = registry.get(RepositoryModule.class).getRepository();
		repository.subscribe(this::reload, true);
	}

	private void reload(RolePlayData data) {

	}
}
