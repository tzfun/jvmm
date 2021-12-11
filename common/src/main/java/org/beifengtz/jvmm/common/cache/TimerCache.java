package org.beifengtz.jvmm.common.cache;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:19 2021/05/11
 *
 * @author beifengtz
 */
public interface TimerCache<K,V> extends Cacheable<K,V> {

    void put(K key,V val, Long expireMilli);

    V getOrDefault(K key, V val, Long expireMilli);
}
