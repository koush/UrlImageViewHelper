package com.koushikdutta.test;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

public class WeakReferenceHashTable<K,V> {
    Hashtable<K, WeakReference<V>> mTable = new Hashtable<K, WeakReference<V>>();
    
    public V put(K key, V value) {
        WeakReference<V> old = mTable.put(key, new WeakReference<V>(value));
        if (old == null)
            return null;
        return old.get();
    }
    
    public V get(K key) {
        WeakReference<V> val = mTable.get(key);
        if (val == null)
            return null;
        V ret = val.get();
        if (ret == null)
            mTable.remove(key);
        return ret;
    }
}
