package ru.atlant.roleplay.board.provider;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import ru.atlant.roleplay.board.impl.IRecordData;

public class PlaceholdersAPIPlaceholderProvider implements PlaceholderProvider {

    @Override
    public IRecordData transform(Player player, String line) {
        return () -> PlaceholderAPI.setPlaceholders(player, line);
    }
}
