package ru.atlant.roleplay.board.provider;

import org.bukkit.entity.Player;
import ru.atlant.roleplay.board.impl.IRecordData;

public interface PlaceholderProvider {

    IRecordData transform(Player player, String line);

}
