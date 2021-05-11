package org.beifengtz.jvmm.tools.cache;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:30 2021/05/11
 *
 * @author beifengtz
 */
public class LocalTimerCache<K, V> implements TimerCache<K, V> {
    private final ConcurrentHashMap<K, WeakReference<V>> CACHE;
    private final ConcurrentHashMap<K, Long> TIME_COUNTER;

    private LocalTimerCache() {
        CACHE = new ConcurrentHashMap<>(32);
        TIME_COUNTER = new ConcurrentHashMap<>(32);
    }

    public static <K, V> LocalTimerCache<K, V> newInstance() {
        return new LocalTimerCache<K, V>();
    }

    @Override
    public V get(K k) {
        WeakReference<V> vw = CACHE.get(k);
        if (Objects.nonNull(vw)) {
            Long expire = TIME_COUNTER.get(k);
            if (Objects.nonNull(expire)) {
                if (Instant.now().isAfter(Instant.ofEpochMilli(expire))) {
                    remove(k);
                    return null;
                }
            }
            return vw.get();
        }
        return null;
    }

    @Override
    public V getOrDefault(K key, V def) {
        return getOrDefault(key, def, -1L);
    }

    @Override
    public void put(K key, V val) {
        put(key, val, -1L);
    }

    @Override
    public boolean contains(K key) {
        if (CACHE.containsKey(key)) {
            Long expireTimestamp = TIME_COUNTER.get(key);
            if (Objects.nonNull(expireTimestamp)) {
                if (Instant.now().isAfter(Instant.ofEpochMilli(expireTimestamp))) {
                    remove(key);
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void remove(K key) {
        CACHE.remove(key);
        TIME_COUNTER.remove(key);
    }

    @Override
    public void remove(Collection<K> keys) {
        keys.forEach(this::remove);
    }

    @Override
    public void clear() {
        CACHE.clear();
        TIME_COUNTER.clear();
    }

    @Override
    public int size() {
        int size = 0;
        Iterator<Map.Entry<K, WeakReference<V>>> it = CACHE.entrySet().iterator();
        Instant now = Instant.now();
        while (it.hasNext()) {
            K key = it.next().getKey();
            Long expire = TIME_COUNTER.get(key);
            if (Objects.nonNull(expire)) {
                if (now.isAfter(Instant.ofEpochMilli(expire))) {
                    it.remove();
                    TIME_COUNTER.remove(key);
                    continue;
                }
            }
            size++;
        }
        return size;
    }

    @Override
    public void put(K key, V val, Long expireMilli) {
        CACHE.put(key, new WeakReference<>(val));
        if (expireMilli > 0) {
            TIME_COUNTER.put(key, Instant.now().plusMillis(expireMilli).toEpochMilli());
        }
    }

    @Override
    public V getOrDefault(K key, V val, Long expireMilli) {
        V v = get(key);
        if (Objects.isNull(v)) {
            put(key, val, expireMilli);
            v = val;
        }
        return v;
    }
}
