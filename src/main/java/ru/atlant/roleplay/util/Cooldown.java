package ru.atlant.roleplay.util;

import java.util.concurrent.TimeUnit;

public class Cooldown<T> extends Requester<T, Long> {

    public Cooldown(TimeUnit unit, long value) {
        super(unit, value);
    }

    public void put(T key) {
        super.put(key, System.currentTimeMillis() + getUnit().toMillis(getValue()));
    }

    public boolean has(T key) {
        return hasRequest(key);
    }

    public long getTimeLeft(T key) {
        Long val = get(key);
        return val == null ? 0L : val;
    }

    public String getTimeLeftFormatted(T key) {
        return TimeUtil.formatTime(getTimeLeft(key), true);
    }

}
