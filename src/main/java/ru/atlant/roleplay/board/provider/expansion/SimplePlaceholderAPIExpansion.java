package ru.atlant.roleplay.board.provider.expansion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.function.Function;

@AllArgsConstructor
public class SimplePlaceholderAPIExpansion extends PlaceholderExpansion {

    @Getter
    private final String identifier;
    private final Function<Player, String> function;

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "AtlantWorld";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        if (!params.equals(identifier))
            return null;
        if (p == null)
            return "null";
        return function.apply(p);
    }
}
