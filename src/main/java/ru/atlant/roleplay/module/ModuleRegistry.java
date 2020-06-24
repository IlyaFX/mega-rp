package ru.atlant.roleplay.module;

import java.util.HashMap;
import java.util.Map;

public class ModuleRegistry {

	private final Map<Class<? extends Module>, Module> moduleMap = new HashMap<>();

	public <T extends Module> T get(Class<T> clazz) {
		return (T) moduleMap.get(clazz);
	}

	public void set(Class<? extends Module> clazz, Module module) {
		moduleMap.put(clazz, module);
	}

}
