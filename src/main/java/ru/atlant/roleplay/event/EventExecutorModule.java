package ru.atlant.roleplay.event;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class EventExecutorModule implements Module, Listener {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    @Override
    public void onEnable() {
    }

    public <T extends Event> Listener registerListener(Class<T> eventClass, Consumer<T> listener, EventPriority ordinal, boolean ignoreCancelled) {
        return registerListener(eventClass, __ -> true, listener, ordinal, ignoreCancelled);
    }

    public <T extends Event> Listener registerListener(Class<T> eventClass, Predicate<T> filter, Consumer<T> listener, EventPriority ordinal, boolean ignoreCancelled) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(ordinal, "ordinal");
        rolePlay.getServer().getPluginManager().registerEvent(eventClass, this, ordinal, (__, event) -> {
            if (filter.test((T) event)) {
                listener.accept((T) event);
            }
        }, rolePlay, ignoreCancelled);
        return this;
    }
}
