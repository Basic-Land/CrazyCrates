package com.badbones69.crazycrates.paper.api.objects.gacha.util;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class TickedCache<K, V> implements Cache<K, V> {
    private final Cache<K, V> cache;

    public TickedCache(int ticks) {
        this.cache = CacheBuilder
                .newBuilder()
                .ticker(new Ticker() {
                    @Override
                    public long read() {
                        return Bukkit.getCurrentTick();
                    }
                })
                .expireAfterWrite(ticks, TimeUnit.NANOSECONDS)
                .build();
    }

    @Override
    public @Nullable V getIfPresent(@NonNull Object key) {
        return cache.getIfPresent(key);
    }

    @Override
    public @NonNull V get(@NonNull K key, @NonNull Callable<? extends V> loader) throws ExecutionException {
        return cache.get(key, loader);
    }

    @Override
    public @NonNull ImmutableMap<K, V> getAllPresent(@NonNull Iterable<?> keys) {
        return cache.getAllPresent(keys);
    }

    @Override
    public void put(@NonNull K key, @NonNull V value) {
        cache.put(key, value);
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        cache.putAll(m);
    }

    @Override
    public void invalidate(@NonNull Object key) {
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll(@NonNull Iterable<?> keys) {
        cache.invalidateAll(keys);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public @NonNull CacheStats stats() {
        return cache.stats();
    }

    @Override
    public @NonNull ConcurrentMap<K, V> asMap() {
        return cache.asMap();
    }

    @Override
    public void cleanUp() {
        cache.cleanUp();
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V value = getIfPresent(key);
        if (value == null) {
            value = mappingFunction.apply(key);
            put(key, value);
        }
        return value;
    }
}
