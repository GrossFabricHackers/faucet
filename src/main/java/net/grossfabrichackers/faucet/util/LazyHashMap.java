package net.grossfabrichackers.faucet.util;

import java.util.HashMap;
import java.util.function.Function;

public class LazyHashMap<K, V> extends HashMap<K, V> {

    private final Function<K, V> factory;

    public LazyHashMap(Function<K, V> factory) {
        this.factory = factory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        V obj = super.get(key);
        if(obj == null) {
            K actualKey = (K) key;
            obj = factory.apply(actualKey);
            put(actualKey, obj);
        }
        return obj;
    }

}
