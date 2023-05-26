package org.beifengtz.jvmm.common.util.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * description: TODO
 * date: 17:01 2023/5/26
 *
 * @author beifengtz
 */
public class MultiMap<K, V> {

    private final Map<K, List<V>> map = new HashMap<>();

    public static <K, V> MultiMap<K, V> of(K k, V v) {
        MultiMap<K, V> multiMap = new MultiMap<>();
        multiMap.put(k, v);
        return multiMap;
    }

    public static <K, V> MultiMap<K, V> of(K k1, V v1, K k2, V v2) {
        MultiMap<K, V> multiMap = new MultiMap<>();
        multiMap.put(k1, v1);
        multiMap.put(k2, v2);
        return multiMap;
    }

    public int size() {
        int size = 0;
        for (Entry<K, List<V>> en : map.entrySet()) {
            size += en.getValue().size();
        }
        return size;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public boolean containsValue(V value) {
        for (Entry<K, List<V>> en : map.entrySet()) {
            if (en.getValue().contains(value)) {
                return true;
            }
        }
        return false;
    }

    public List<V> get(K key) {
        return map.get(key);
    }

    public V put(K key, V value) {
        map.computeIfAbsent(key, o -> new ArrayList<>()).add(value);
        return value;
    }

    public List<V> remove(K key) {
        return map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public List<V> values() {
        List<V> res = new ArrayList<>();
        for (Entry<K, List<V>> en : map.entrySet()) {
            res.addAll(en.getValue());
        }
        return res;
    }

    public Set<Entry<K, List<V>>> entrySet() {
        return map.entrySet();
    }
}
