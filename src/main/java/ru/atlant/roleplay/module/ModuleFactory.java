package ru.atlant.roleplay.module;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.reflections.Reflections;
import ru.atlant.roleplay.RolePlay;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleFactory {

    @Getter
    private final ModuleRegistry registry = new ModuleRegistry();
    private final Map<Class<? extends Module>, Module> activeModules = new HashMap<>();

    public void registerAll(RolePlay inst) {
        Reflections reflections = new Reflections("ru.atlant.roleplay");
        Map<Class<? extends Module>, Class<? extends Module>[]> depends = new HashMap<>();
        reflections.getSubTypesOf(Module.class).forEach(clz -> {
            try {
                Constructor constructor = clz.getConstructor(RolePlay.class, ModuleRegistry.class);
                constructor.setAccessible(true);
                Module module = (Module) constructor.newInstance(inst, registry);
                if (module.isEnabled()) {
                    activeModules.put(clz, module);
                    registry.set(clz, module);
                    if (clz.isAnnotationPresent(LoadAfter.class)) {
                        LoadAfter after = clz.getAnnotation(LoadAfter.class);
                        depends.put(clz, after.clazz());
                    }
                    System.out.println("Module " + clz.getName() + " loaded!");
                }
            } catch (Exception e) {
                System.out.println("Error loading module " + clz.getName() + "!");
                e.printStackTrace();
            }
        });
        List<Module> notLoadedModules = new ArrayList<>(activeModules.values());
        while (!notLoadedModules.isEmpty()) {
            notLoadedModules.removeIf(module -> {
                Class<? extends Module>[] arr = depends.get(module.getClass());
                boolean passed = arr == null || arr.length == 0;
                if (!passed) {
                    for (Class<? extends Module> clazz : arr) {
                        if (notLoadedModules.stream().anyMatch(m -> m.getClass().equals(clazz)))
                            return false;
                    }
                }
                try {
                    module.onEnable();
                    System.out.println("Module " + module.getClass().getName() + " enabled!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Error enabling module " + module.getClass().getName() + ": " + ex.getMessage());
                }
                return true;
            });
        }
        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                activeModules.forEach((clz, m) -> {
                    try {
                        m.handleJoin(event.getPlayer());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                activeModules.forEach((clz, m) -> {
                    try {
                        m.handleQuit(event.getPlayer());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

        }, inst);
    }

    public void onDisable() {
        activeModules.forEach((clz, module) -> {
            try {
                module.onDisable();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

}
