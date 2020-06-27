package ru.atlant.roleplay.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@UtilityClass
public class WorldUtil {

    public double distance(Location source, Location target) {
        if (!source.getWorld().equals(target.getWorld()))
            return Double.POSITIVE_INFINITY;
        return source.distance(target);
    }

    public double distance(Player player, Location target) {
        return distance(player.getLocation(), target);
    }

    public double distance(Player source, Player target) {
        return distance(source.getLocation(), target.getLocation());
    }

}
