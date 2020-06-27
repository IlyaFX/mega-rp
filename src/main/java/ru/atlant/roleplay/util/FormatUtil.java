package ru.atlant.roleplay.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class FormatUtil {

    public String pluralFormRu(int count, String form1, String form2, String form5) {
        count = count % 100;
        int count1 = count % 10;
        return count > 10 && count < 20 ? form5 : count1 > 1 && count1 < 5 ? form2 : count1 == 1 ? form1 : form5;
    }

    public String info(InfoLevel infoLevel, String string) {
        return infoLevel.color + "[i] " + Colors.reset + string;
    }

    public String fine(String string) {
        return info(InfoLevel.FINE, string);
    }

    public String warn(String string) {
        return info(InfoLevel.WARN, string);
    }

    public String error(String string) {
        return info(InfoLevel.ERROR, string);
    }

    public String error(Throwable t) {
        return error(throwable(t));
    }

    public String throwable(Throwable t) {
        val trace = t.getStackTrace();
        return t.getClass().getSimpleName() + " : " + t.getMessage()
                + ((trace.length > 0) ? " @ " + t.getStackTrace()[0].getClassName() + ":" + t.getStackTrace()[0].getLineNumber() : "");
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum InfoLevel {
        FINE(Colors.cLime),
        WARN(Colors.cOrange),
        ERROR(Colors.cPink);

        private final String color;
    }

}
