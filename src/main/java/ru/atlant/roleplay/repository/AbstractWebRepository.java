package ru.atlant.roleplay.repository;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import ru.atlant.roleplay.util.DataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractWebRepository<T> extends WebRepository<T> {

	private final List<Consumer<T>> subscribes = new ArrayList<>();
	private final Plugin plugin;
	private String oldValue;
	private T value;
	private long lastReload;
	private final long reloadTime;

	public AbstractWebRepository(Plugin plugin, String url, TimeUnit reloadTimeUnit, long reloadTime) {
		super(url);
		this.plugin = plugin;
		this.reloadTime = reloadTimeUnit.toMillis(reloadTime);
	}

	public void init() {
		reload();
		Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			if (System.currentTimeMillis() - lastReload > reloadTime)
				reload();
		}, 20, 20);
	}

	public void subscribe(Consumer<T> update, boolean callNow) {
		subscribes.add(update);
		if (value != null && callNow)
			update.accept(value);
	}

	private void reload() {
		connect()
				.thenAccept(json -> {
					if (!json.equals(oldValue)) {
						update(DataUtils.GSON.fromJson(json, getClazz()));
						oldValue = json;
					}
				})
				.exceptionally(thr -> {
					System.out.println("Can't get data for url: " + getUrl());
					thr.printStackTrace();
					return null;
				});
		lastReload = System.currentTimeMillis();
	}

	public void update(T newValue) {
		value = newValue;
		subscribes.forEach(consumer -> consumer.accept(newValue));
	}

	public abstract Class<T> getClazz();

}
