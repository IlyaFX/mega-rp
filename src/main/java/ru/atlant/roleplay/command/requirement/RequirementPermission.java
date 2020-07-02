package ru.atlant.roleplay.command.requirement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.Stream;

public class RequirementPermission {

    public static void permission(UUID user, String... perms) {
        Player player = Bukkit.getPlayer(user);
        if (Stream.of(perms).allMatch(player::hasPermission))
            return;
        throw new RuntimeException("У вас недостаточно прав на выполнение этой команды!");
    }

}
