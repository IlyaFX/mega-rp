package ru.atlant.roleplay.work.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.command.CommandModule;
import ru.atlant.roleplay.command.requirement.RequirementPermission;
import ru.atlant.roleplay.command.type.TypeTime;
import ru.atlant.roleplay.command.type.TypeUser;
import ru.atlant.roleplay.config.ConfigModule;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.user.UsersModule;
import ru.atlant.roleplay.util.FormatUtil;
import ru.atlant.roleplay.util.WorldUtil;

import java.util.UUID;

import static ru.atlant.roleplay.command.CommandHelper.*;

@RequiredArgsConstructor
@LoadAfter(clazz = {CommandModule.class, ConfigModule.class, UsersModule.class})
public class ArrestCommand implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    private int starMinutes;
    private double distance;
    private String arrestPermission;
    private String arrestForcePermission;

    @Override
    public void onEnable() {
        UsersModule users = registry.get(UsersModule.class);
        registry.get(ConfigModule.class)
                .subscribe("arrest-distance", dis -> distance = dis, Double::parseDouble, 10.0, true)
                .subscribe("arrest-permission", permission -> arrestPermission = permission, "roleplay.arrest", true)
                .subscribe("arrest-force-permission", permission -> arrestForcePermission = permission, "roleplay.arrest.force", true)
                .subscribe("arrest-star-minutes", minutes -> starMinutes = minutes, Integer::parseInt, 10, true);
        registry.get(CommandModule.class).getDispatcher().register(
                literal("arrest")
                        .requires(RequirementPermission.permission(arrestPermission))
                        .then(argument("Игрок", TypeUser.user(true))
                                .executes(wrap(ctx -> {
                                    UUID source = ctx.getSource();
                                    UUID target = ctx.getArgument("Игрок", UUID.class);
                                    Player sourcePlayer = Bukkit.getPlayer(source), targetPlayer = Bukkit.getPlayer(target);
                                    UsersModule.UserData userData = users.dataUnsafe(target);
                                    if (WorldUtil.distance(sourcePlayer, targetPlayer) > distance) {
                                        sourcePlayer.sendMessage(FormatUtil.error("Вы слишком далеко от игрока!"));
                                        return;
                                    }
                                    if (userData.inPrison()) {
                                        sourcePlayer.sendMessage(FormatUtil.error("Игрок уже в тюрьме!"));
                                        return;
                                    }
                                    if (userData.getStars() <= 0) {
                                        sourcePlayer.sendMessage(FormatUtil.error("У игрока нет звёзд розыска, вы не можете его посадить!"));
                                        return;
                                    }
                                    long time = (userData.getStars() * starMinutes) * 60000;
                                    users.replaceViolation(target, 0, time);
                                    sourcePlayer.sendMessage(FormatUtil.fine("Игрок посажен в тюрьму"));
                                    System.out.println("ARREST EXECUTED! " + sourcePlayer.getName() + " > " + targetPlayer.getName());
                                })))
        );
        registry.get(CommandModule.class).getDispatcher().register(
                literal("arrest-force")
                        .requires(RequirementPermission.permission(arrestForcePermission))
                        .then(argument("Игрок", TypeUser.user(true))
                                .then(argument("Время", TypeTime.time(false))
                                        .executes(wrap(ctx -> {
                                            UUID source = ctx.getSource();
                                            UUID target = ctx.getArgument("Игрок", UUID.class);
                                            Player sourcePlayer = Bukkit.getPlayer(source);
                                            if (users.dataUnsafe(target).inPrison()) {
                                                sourcePlayer.sendMessage(FormatUtil.error("Игрок уже в тюрьме!"));
                                                return;
                                            }
                                            long time = ctx.getArgument("Время", Long.class);
                                            users.replaceViolation(target, 0, time);
                                            sourcePlayer.sendMessage(FormatUtil.fine("Игрок посажен в тюрьму"));
                                            System.out.println("FORCE ARREST EXECUTED! " + sourcePlayer.getName() + " > " + Bukkit.getPlayer(target).getName() + " for " + time);
                                        }))))
        );
    }
}
