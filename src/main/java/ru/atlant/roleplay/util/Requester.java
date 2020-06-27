package ru.atlant.roleplay.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

public class Requester<K, V> {

    private final Cache<K, V> cache;

    @Getter
    private final TimeUnit unit;
    @Getter
    private final long value;

    public Requester(TimeUnit unit, long value) {
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(value, unit).build();
        this.unit = unit;
        this.value = value;
    }

    public boolean hasRequest(K key) {
        return get(key) != null;
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public V get(K key) {
        return cache.getIfPresent(key);
    }

    public V getAndInvalidate(K key) {
        V val = get(key);
        invalidate(key);
        return val;
    }

    public void invalidate(K key) {
        cache.invalidate(key);
    }

}
