package ru.atlant.roleplay.module;

import org.bukkit.entity.Player;

public interface Module {

	void onEnable();

	default void onDisable() {
	}

	default void handleJoin(Player player) {
	}

	default void handleQuit(Player player) {
	}

	default boolean isEnabled() {
		return true;
	}

}
