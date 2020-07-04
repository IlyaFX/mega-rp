package ru.atlant.roleplay.command.type;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TypeUser implements ArgumentType<UUID> {

    private static final DynamicCommandExceptionType INVALID_ONLINE = new DynamicCommandExceptionType(o -> new LiteralMessage("Игрок не онлайн: " + o));
    private static final DynamicCommandExceptionType INVALID_OFFLINE = new DynamicCommandExceptionType(o -> new LiteralMessage("Игрока не существует: " + o));

    private final boolean online;

    public static TypeUser user(boolean online) {
        return new TypeUser(online);
    }

    @Override
    public UUID parse(StringReader stringReader) throws CommandSyntaxException {
        String name = stringReader.readUnquotedString();
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (player == null)
            throw INVALID_OFFLINE.createWithContext(stringReader, name);
        if (online) {
            if (!player.isOnline())
                throw INVALID_ONLINE.createWithContext(stringReader, name);
        }
        return player.getUniqueId();
    }

    @Override
    public Collection<String> getExamples() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}
