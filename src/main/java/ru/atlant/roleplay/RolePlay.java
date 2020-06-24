package ru.atlant.roleplay;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.atlant.roleplay.module.ModuleFactory;
import ru.atlant.roleplay.module.ModuleRegistry;

@Getter
public class RolePlay extends JavaPlugin {

	private final ModuleFactory factory = new ModuleFactory();
	private ModuleRegistry registry;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		factory.registerAll(this);
		this.registry = factory.getRegistry();
	}

	@Override
	public void onDisable() {
		factory.onDisable();
	}

}
