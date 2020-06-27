package ru.atlant.roleplay.command.type;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import lombok.val;

import java.util.Arrays;
import java.util.Collection;

public final class TypeTime implements ArgumentType<Long> {

    private static final Collection<String> EXAMPLES = Arrays.asList("30m", "2h", "1d");

    private static final DynamicCommandExceptionType INVALID_MULTIPLIER = new DynamicCommandExceptionType(o -> new LiteralMessage("Неверный множитель " + o));
    private static final DynamicCommandExceptionType INVALID_NUMBER = new DynamicCommandExceptionType(o -> new LiteralMessage("Неверное число " + o));
    private final boolean forever;

    private TypeTime(boolean forever) {
        this.forever = forever;
    }

    public static TypeTime time(boolean forever) {
        return new TypeTime(forever);
    }

    @Override
    public Long parse(StringReader reader) throws CommandSyntaxException {
        val arg = reader.readUnquotedString();
        int mult;
        switch (arg.substring(arg.length() - 1)) {
            case "s":
                mult = 1;
                break;
            case "m":
                mult = 60;
                break;
            case "h":
                mult = 60 * 60;
                break;
            case "d":
            case "D":
                mult = 60 * 24 * 60;
                break;
            case "M":
                mult = 60 * 24 * 60 * 30;
                break;
            case "Y":
                mult = 60 * 24 * 60 * 365;
                break;
            default:
                throw INVALID_MULTIPLIER.createWithContext(reader, arg);
        }
        val number = arg.substring(0, arg.length() - 1);
        long time;
        try {
            time = Long.parseLong(number, 10);
        } catch (NumberFormatException e) {
            throw INVALID_NUMBER.createWithContext(reader, number);
        }
        if (forever && time == 0L) return -1L;
        return time * mult * 1000L;

    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
