package ru.atlant.roleplay.work.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.command.CommandModule;
import ru.atlant.roleplay.command.type.TypeUser;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.user.UsersModule;
import ru.atlant.roleplay.work.Job;
import ru.atlant.roleplay.work.WorksModule;

import java.util.Objects;
import java.util.UUID;

import static ru.atlant.roleplay.command.CommandHelper.*;

@RequiredArgsConstructor
@LoadAfter(clazz = {CommandModule.class, UsersModule.class, WorksModule.class})
public class WorksCommand implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    @Override
    public void onEnable() {
        UsersModule users = registry.get(UsersModule.class);
        WorksModule works = registry.get(WorksModule.class);
        CommandDispatcher<UUID> commands = registry.get(CommandModule.class).getDispatcher();
        commands.register(
                literal("setfracjob")
                        .then(argument("Игрок", TypeUser.user(true))
                                .then(argument("Фракция", StringArgumentType.word())
                                        .then(argument("Работа", StringArgumentType.word())
                                                .executes(wrap(ctx -> {
                                                    UUID source = ctx.getSource(), target = ctx.getArgument("Игрок", UUID.class);
                                                    String fraction = ctx.getArgument("Фракция", String.class), job = ctx.getArgument("Работа", String.class);
                                                    Job found = Objects.requireNonNull(works.find(fraction, job), "Job is null! (Why?)");
                                                    users.replaceJob(target, found);
                                                    Bukkit.getPlayer(source).sendMessage("Установлена работа fracId:" + found.getFraction().getId() + ", jobId:" + found.getId());
                                                })))))
        );
    }
}
