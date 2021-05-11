package org.beifengtz.jvmm.tools.cache;

import java.util.Collection;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:19 2021/05/11
 *
 * @author beifengtz
 */
public interface Cacheable<K, V> {
    V get(K key);

    V getOrDefault(K key, V def);

    void put(K key, V val);

    boolean contains(K key);

    void remove(K key);

    void remove(Collection<K> keys);

    void clear();

    int size();
}
