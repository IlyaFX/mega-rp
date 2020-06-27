package ru.atlant.roleplay.command.requirement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class RequirementPermission implements Predicate<UUID> {

    private List<String> permissions;

    public RequirementPermission(String... perms) {
        this.permissions = Arrays.asList(perms);
    }

    public static RequirementPermission permission(String... perms) {
        return new RequirementPermission(perms);
    }

    @Override
    public boolean test(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return permissions.stream().allMatch(player::hasPermission);
    }
}
