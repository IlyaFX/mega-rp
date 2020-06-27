package ru.atlant.roleplay.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.repository.RepositoryModule;
import ru.atlant.roleplay.repository.impl.RolePlayDataRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
@LoadAfter(clazz = {RepositoryModule.class})
public class ConfigModule implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    @Getter
    private Map<String, String> config = new HashMap<>(1);

    private final Map<String, List<ConfigSubscriber>> subscribesMap = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        RolePlayDataRepository repository = registry.get(RepositoryModule.class).getRepository();
        repository.subscribe(data -> handleUpdate(data.getConfig()), true);
    }

    private void handleUpdate(Map<String, String> configuration) {
        configuration.forEach((key, value) -> {
            String oldVal = config.get(key);
            if ((value == null && oldVal != null) || (value != null && !value.equals(oldVal))) {
                Optional
                        .ofNullable(subscribesMap.get(key))
                        .ifPresent(subscribers -> subscribers.forEach(subscriber -> executeUpdate(subscriber, value)));
            }
        });
        config = new ConcurrentHashMap<>(configuration);
    }

    public ConfigModule subscribe(String key, Consumer<String> consumer, String def, boolean callNow) {
        return subscribe(key, consumer, val -> val, def, callNow);
    }

    public <T> ConfigModule subscribe(String key, Consumer<T> consumer, Function<String, T> valueTransformer, T def, boolean callNow) {
        ConfigSubscriber<T> subscriber = new ConfigSubscriber<>(key, consumer, valueTransformer, def);
        subscribesMap.computeIfAbsent(key, __ -> new ArrayList<>()).add(subscriber);
        if (callNow) {
            executeUpdate(subscriber, config.get(key));
        }
        return this;
    }

    private void executeUpdate(ConfigSubscriber subscriber, String value) {
        Object val = constructGet(subscriber.getKey(), value, subscriber.getDefaultValue(), subscriber.getValueTransformer());
        try {
            subscriber.getValueConsumer().accept(val);
        } catch (Exception ex) {
            System.out.println("Error while handling config subscriber on " + subscriber.getKey() + " : " + value + " (key:value)");
            ex.printStackTrace();
        }
    }

    public long getLong(String key, long def) {
        return constructGet(key, config.get(key), def, Long::parseLong);
    }

    public int getInt(String key, int def) {
        return constructGet(key, config.get(key), def, Integer::parseInt);
    }

    public String getString(String key, String def) {
        return config.getOrDefault(key, def);
    }

    private <T> T constructGet(String key, String val, T def, Function<String, T> constructor) {
        if (val != null) {
            try {
                return constructor.apply(val);
            } catch (Exception ex) {
                System.out.println("Exception on config transforming! Key: " + key + ", value: " + val);
                ex.printStackTrace();
                return def;
            }
        }
        return def;
    }

    @AllArgsConstructor
    @Getter
    private static class ConfigSubscriber<T> {

        private final String key;
        private final Consumer<T> valueConsumer;
        private final Function<String, T> valueTransformer;
        private final T defaultValue;

    }
}
