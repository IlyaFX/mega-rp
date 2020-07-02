package ru.atlant.roleplay.command.requirement;

import com.mojang.brigadier.RequirementException;
import org.bukkit.Bukkit;
import ru.atlant.roleplay.util.Cooldown;

import java.util.UUID;

public class RequirementCooldown {

    public static void cooldown(Cooldown<UUID> cooldown, UUID user) {
        if (!cooldown.has(user) || Bukkit.getPlayer(user).hasPermission("roleplay.cooldown.bypass"))
            return;
        throw new RequirementException("Слишком часто! Вы сможете использовать команду вновь через " + cooldown.getTimeLeftFormatted(user));
    }

}
