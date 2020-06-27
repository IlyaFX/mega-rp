package ru.atlant.roleplay.work.command;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.command.CommandModule;
import ru.atlant.roleplay.command.requirement.RequirementPermission;
import ru.atlant.roleplay.command.requirement.RequirementRequest;
import ru.atlant.roleplay.command.type.TypeTime;
import ru.atlant.roleplay.command.type.TypeUser;
import ru.atlant.roleplay.config.ConfigModule;
import ru.atlant.roleplay.economy.EconomyModule;
import ru.atlant.roleplay.economy.provider.EconomyProvider;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.user.UsersModule;
import ru.atlant.roleplay.util.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static ru.atlant.roleplay.command.CommandHelper.*;

@RequiredArgsConstructor
@LoadAfter(clazz = {CommandModule.class, ConfigModule.class, UsersModule.class, EconomyModule.class})
public class ArrestCommand implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    private int starMinutes;
    private double arrestDistance;
    private String arrestPermission;
    private String arrestForcePermission;
    private double unarrestDistance;
    private String unarrestPermission;
    private String unarrestForcePermission;
    private double unarrestPricePerMinute;
    private double unarrestProcent;

    private UserRequester unarrestRequester;

    @Override
    public void onEnable() {
        EconomyProvider economy = registry.get(EconomyModule.class).getProvider();
        UsersModule users = registry.get(UsersModule.class);
        registry.get(ConfigModule.class)
                .subscribe("arrest-distance", dis -> arrestDistance = dis, Double::parseDouble, 10.0, true)
                .subscribe("arrest-permission", permission -> arrestPermission = permission, "roleplay.arrest", true)
                .subscribe("arrest-force-permission", permission -> arrestForcePermission = permission, "roleplay.arrest.force", true)
                .subscribe("arrest-star-minutes", minutes -> starMinutes = minutes, Integer::parseInt, 10, true)
                .subscribe("unarrest-permission", permission -> unarrestPermission = permission, "roleplay.unarrest", true)
                .subscribe("unarrest-force-permission", permission -> unarrestForcePermission = permission, "roleplay.unarrest.force", true)
                .subscribe("unarrest-distance", dis -> unarrestDistance = dis, Double::parseDouble, 10.0, true)
                .subscribe("unarrest-requester", secs -> unarrestRequester = new UserRequester(TimeUnit.SECONDS, secs), Integer::parseInt, 60, true)
                .subscribe("unarrest-price", price -> unarrestPricePerMinute = price, Double::parseDouble, 100.0, true)
                .subscribe("unarrest-procent", proc -> unarrestProcent = proc / 100, Double::parseDouble, 50.0, true);
        registry.get(CommandModule.class).getDispatcher().register(
                literal("arrest")
                        .requires(RequirementPermission.permission(arrestPermission))
                        .then(argument("Игрок", TypeUser.user(true))
                                .executes(wrap(ctx -> {
                                    UUID source = ctx.getSource();
                                    UUID target = ctx.getArgument("Игрок", UUID.class);
                                    Player sourcePlayer = Bukkit.getPlayer(source), targetPlayer = Bukkit.getPlayer(target);
                                    UsersModule.UserData userData = users.dataUnsafe(target);
                                    if (WorldUtil.distance(sourcePlayer, targetPlayer) > arrestDistance) {
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
                                }))
                                .then(argument("Время", TypeTime.time(false))
                                        .requires(RequirementPermission.permission(arrestForcePermission))
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
        registry.get(CommandModule.class).getDispatcher().register(
                literal("unarrest")
                        .then(literal("accept")
                                .requires(RequirementRequest.request(unarrestRequester))
                                .executes(wrap(ctx -> {
                                    UUID source = ctx.getSource();
                                    UUID target = unarrestRequester.getAndInvalidate(source);
                                    Player sourcePlayer = Bukkit.getPlayer(source), targetPlayer = Bukkit.getPlayer(target);
                                    UsersModule.UserData data = users.dataUnsafe(source);
                                    double price = unarrestPrice(data);
                                    if (price > 0) {
                                        if (!economy.has(source, price)) {
                                            sourcePlayer.sendMessage(FormatUtil.error("Недостаточно денег!"));
                                            return;
                                        }
                                        if (targetPlayer == null) {
                                            sourcePlayer.sendMessage(FormatUtil.error("Адвокат ушёл!"));
                                            return;
                                        }
                                        economy.take(source, price);
                                        double targetSalary = price * unarrestProcent;
                                        economy.deposit(target, targetSalary);
                                        // TODO: казна?
                                        targetPlayer.sendMessage(FormatUtil.fine("Вы освободили игрока " + sourcePlayer.getName() + " и получили " + Colors.cLime + NumberUtil.FORMAT.format(targetSalary)));
                                        sourcePlayer.sendMessage(FormatUtil.fine("Вы освободились из тюрьмы за " + Colors.cLime + NumberUtil.FORMAT.format(price)));
                                        users.replaceViolation(source, 0, 0);
                                    }
                                })))
                        .then(literal("decline")
                                .requires(RequirementRequest.request(unarrestRequester))
                                .executes(wrap(ctx -> {
                                    Player source = Bukkit.getPlayer(ctx.getSource());
                                    Player target = Bukkit.getPlayer(unarrestRequester.getAndInvalidate(ctx.getSource()));
                                    source.sendMessage(FormatUtil.fine("Вы отказались от услуг адвоката"));
                                    if (target != null)
                                        target.sendMessage(FormatUtil.warn("Игрок " + source.getName() + " отказался от ваших услуг"));
                                })))
                        .requires(RequirementPermission.permission(unarrestPermission))
                        .then(argument("Игрок", TypeUser.user(true))
                                .executes(wrap(ctx -> {
                                    UUID source = ctx.getSource();
                                    UUID target = ctx.getArgument("Игрок", UUID.class);
                                    Player sourcePlayer = Bukkit.getPlayer(source), targetPlayer = Bukkit.getPlayer(target);
                                    UsersModule.UserData data = users.dataUnsafe(target);
                                    if (!data.inPrison()) {
                                        sourcePlayer.sendMessage(FormatUtil.error("Игрок не в тюрьме!"));
                                        return;
                                    }
                                    if (sourcePlayer.hasPermission(unarrestForcePermission)) {
                                        users.replaceViolation(target, 0, 0);
                                        sourcePlayer.sendMessage(FormatUtil.fine("Игрок выпущен из тюрьмы!"));
                                        return;
                                    }
                                    if (WorldUtil.distance(sourcePlayer, targetPlayer) > unarrestDistance) {
                                        sourcePlayer.sendMessage(FormatUtil.error("Вы слишком далеко от игрока!"));
                                        return;
                                    }
                                    if (unarrestRequester.hasRequest(target)) {
                                        sourcePlayer.sendMessage(FormatUtil.warn("У игрока уже есть предложение от адвоката! Подождите чуть-чуть :з"));
                                        return;
                                    }
                                    double price = unarrestPrice(data);
                                    if (!economy.has(target, price)) {
                                        sourcePlayer.sendMessage(FormatUtil.warn("У игрока слишком мало денег, даже на адвоката не хватает :c"));
                                        return;
                                    }
                                    unarrestRequester.put(target, source);
                                    sourcePlayer.sendMessage(FormatUtil.fine("Предложение об оказании адвокатских услуг отправлено!"));
                                    BaseComponent[] components =
                                            new ComponentBuilder("==============\n").color(ChatColor.AQUA)
                                                    .append("     \n")
                                                    .append("Игрок " + sourcePlayer.getName() + " предлагает вам выйти из тюрьмы за " + Colors.cLime + NumberUtil.FORMAT.format(price) + "\n")
                                                    .append("[Принять]").color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unarrest accept")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("Нажмите чтобы принять предложение")}))
                                                    .append("    ")
                                                    .append("[Отклонить]\n").color(ChatColor.RED).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unarrest decline")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("Нажмите чтобы отклонить предложение")}))
                                                    .append("У вас " + unarrestRequester.getValue() + " секунд чтобы ответить").color(ChatColor.YELLOW)
                                                    .append("      \n")
                                                    .append("==============").color(ChatColor.AQUA).create();
                                    targetPlayer.sendMessage(components);
                                })))
        );
    }

    private double unarrestPrice(UsersModule.UserData data) {
        return unarrestPricePerMinute * (double) ((data.getPrison() - System.currentTimeMillis()) / 60000);
    }
}
