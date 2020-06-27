package ru.atlant.roleplay.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtil {

    public String formatTime(long time, boolean string) {
        long hours = time / 3600000;
        long minutes = (time % 3600000) / 60000;

        long seconds = Math.max(((time % 3600000 % 60000) / 1000), 0);

        if (string)
            return (hours != 0 ? hours + " ч. " : "") + (minutes != 0 ? minutes + " мин. " : "") + (seconds + " сек.");
        return numbStr(hours) + ":" + numbStr(minutes) + ":" + numbStr(seconds);
    }

    private String numbStr(long l) {
        return (l >= 10 ? Long.toString(l) : "0" + l);
    }

}
