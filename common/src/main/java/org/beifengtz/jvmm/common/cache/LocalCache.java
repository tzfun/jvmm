package org.beifengtz.jvmm.common.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:20 2021/05/11
 *
 * @author beifengtz
 */
public class LocalCache<K, V> implements Cacheable<K, V> {
    private final com.google.common.cache.Cache<K, V> localCache;

    private LocalCache(boolean expireFromWrite, int expireSec, int maximumSize,RemovalListener<K, V> listener) {
        CacheBuilder<Object, Object> rb = CacheBuilder.newBuilder()
                .maximumSize(maximumSize < 0 ? Integer.MAX_VALUE : maximumSize);
        if (Objects.nonNull(listener)){
            rb.removalListener(listener);
        }

        if (expireFromWrite) {
            rb.expireAfterWrite(expireSec, TimeUnit.SECONDS);
        } else {
            rb.expireAfterAccess(expireSec, TimeUnit.SECONDS);
        }

        localCache = rb.build();
    }

    public static <K, V> LocalCache<K, V> newInstance(boolean expireFromWrite, int expireSec, int maximumSize) {
        return new LocalCache<>(expireFromWrite, expireSec, maximumSize,null);
    }
    public static <K, V> LocalCache<K, V> newInstance(boolean expireFromWrite, int expireSec, int maximumSize,RemovalListener<K, V> listener) {
        return new LocalCache<>(expireFromWrite, expireSec, maximumSize,listener);
    }

    @Override
    public V get(K key) {
        return localCache.getIfPresent(key);
    }

    @Override
    public V getOrDefault(K key, V def) {
        V v = get(key);
        if (Objects.isNull(v)) {
            v = def;
            put(key, def);
        }
        return v;
    }

    @Override
    public void put(K key, V val) {
        localCache.put(key, val);
    }

    @Override
    public boolean contains(K key) {
        return localCache.asMap().containsKey(key);
    }

    @Override
    public void remove(K key) {
        if (Objects.nonNull(key)){
            localCache.invalidate(key);
        }
    }

    @Override
    public void remove(Collection<K> keys) {
        localCache.invalidateAll(keys);
    }

    @Override
    public void clear() {
        localCache.invalidateAll();
    }

    @Override
    public int size() {
        return Math.toIntExact(localCache.size());
    }
}
