package ru.atlant.roleplay.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.experimental.UtilityClass;

import java.util.UUID;
import java.util.function.Consumer;

@UtilityClass
public class CommandHelper {

    public LiteralArgumentBuilder<UUID> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    public <T> RequiredArgumentBuilder<UUID, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public Command<UUID> wrap(Consumer<CommandContext<UUID>> consumer) {
        return context -> {
            consumer.accept(context);
            return 1;
        };
    }

}
